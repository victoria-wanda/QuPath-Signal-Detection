import qupath.lib.projects.ProjectIO
import qupath.lib.projects.Projects
import qupath.lib.images.servers.ImageServers
import qupath.lib.objects.PathObjects
import qupath.lib.roi.ROIs
import qupath.lib.regions.ImagePlane
import qupath.lib.gui.scripting.QPEx
import java.awt.image.BufferedImage

// ==== ARGUMENTY ====
if (args == null || args.length < 2) {
    println "ERROR: Brak ścieżki do obrazka lub folderu"
    return
}
def imagePath = args[0]
def resultDirPath = args[1]

println imagePath
def imageFile = new File(imagePath)

if (!imageFile.exists()) {
    println "ERROR: Obraz nie istnieje: ${imagePath}"
    return
}
println "# Używany obraz: ${imagePath}"

// Ściezki - Do ewentualnej zmiany directory, nowe pliki zapiszą się w folderze resultDir
def projectDir = new File(resultDirPath)
def resultDir = new File("${resultDirPath}/pog")
resultDir.mkdirs()

// Otwieranie lub tworzenie nowego projektu 
def projectFile = new File(projectDir, "project.qpproj")
def myProject = null  // myProject, nazwa aby nie nadpisywać qupatha
if (projectFile.exists()) {
    myProject = ProjectIO.loadProject(projectFile, BufferedImage.class)
    println "# Otworzono istniejący projekt: ${projectFile}"
} else {
    myProject = Projects.createProject(projectDir, BufferedImage.class)
    println "# Utworzono nowy projekt w: ${projectDir}"
}

// Dodaj obraz
def imageEntry = myProject.getImageList().find { it.getImageName() == imageFile.getName() }
if (imageEntry == null) {
    def builder = ImageServers.buildServer(imageFile.getAbsolutePath()).getBuilder()
    imageEntry = myProject.addImage(builder)
    imageEntry.setImageName(imageFile.getName())
    myProject.syncChanges()
    println "# Dodano obraz do projektu: ${imageFile.getName()}"
} else {
    println "# Obraz już istnieje w projekcie: ${imageFile.getName()}"
}

// Przetwarzanie obrazka
def imageData = imageEntry.readImageData()
def hierarchy = imageData.getHierarchy()
def server = imageData.getServer()
QPEx.setBatchProjectAndImage(myProject, imageData)

// USTAWIENIE ROZMIARU PIKSELI 
setPixelSizeMicrons(0.203500, 0.203500)

println "# Przetwarzanie obrazu: ${imageFile.getName()}"

// Adnotacja obrazka
def annotation = PathObjects.createAnnotationObject(
    ROIs.createRectangleROI(0, 0, server.getWidth(), server.getHeight(), ImagePlane.getDefaultPlane())
)
hierarchy.addObject(annotation, false)

// CELL DETECTION
QPEx.setImageType('FLUORESCENCE')
QPEx.selectAnnotations()

// Przypisanie wyniku do zmiennej result
def result = QPEx.runPlugin('qupath.imagej.detect.cells.WatershedCellDetection',
    '{"detectionImage":"Blue","requestedPixelSizeMicrons":0.2035,"backgroundRadiusMicrons":8.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.2,"sigmaMicrons":4.07,"minAreaMicrons":1.66,"maxAreaMicrons":529.9,"threshold":10.0,"watershedPostProcess":true,"cellExpansionMicrons":1.4056,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":true}')
println "# Detekcja komórek zakończona: ${result}"

// Subkomórkowa detekcja
def subcellularResult = QPEx.runPlugin('qupath.imagej.detect.cells.SubcellularDetection',
    '{"detection[Channel 1]":65.0,"detection[Channel 2]":50.0,"detection[Channel 3]":-1.0,"doSmoothing":true,"splitByIntensity":true,"splitByShape":true,"spotSizeMicrons":0.7,"minSpotSizeMicrons":0.5,"maxSpotSizeMicrons":2.0,"includeClusters":true}')
println "# Detekcja sub-komórek zakończona: ${subcellularResult}"

// Sprawdzenie liczby wykrytych obiektów
def detections = hierarchy.getDetectionObjects()
println "# Wykryto łącznie obiektów: ${detections.size()}"

// DEFINICJA BASENAME
def fileName = imageFile.getName()
def lastDot = fileName.lastIndexOf('.')
def baseName = lastDot > 0 ? fileName.substring(0, lastDot) : fileName

// Zapis wyników
def outputFile = new File(resultDir, "${baseName}_measurements.txt")
QPEx.saveDetectionMeasurements(outputFile.getAbsolutePath())
println "# Zapisano pomiary do: ${outputFile}"
