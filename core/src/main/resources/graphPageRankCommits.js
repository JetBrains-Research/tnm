const elem = document.getElementById('3d-graph');

const materialBox = new THREE.MeshLambertMaterial({
    color: "#d6c8a3",
    opacity: 1,
    emissive: 0xc0303
})

const materialSphere = new THREE.MeshLambertMaterial({
    color: "#9eb8e6",
    opacity: 1,
    emissive: 0xc0303
})

const materials = [materialSphere, materialBox]
const meshes = [
    () => new THREE.SphereGeometry(4, 32, 32),
    () => new THREE.BoxGeometry(5, 15, 10),
]

const Graph = ForceGraph3D()(elem)
    .graphData(data)
    .linkWidth(link => link.value)
    .linkColor(link => link.color)
    .linkDirectionalArrowLength(8.5)
    .linkDirectionalArrowRelPos(1)
    .linkCurvature(0.25)
    .linkOpacity(1)
    .nodeOpacity(1)
    .nodeAutoColorBy('color')
    .nodeVal('value')
    .nodeLabel(node => `${node.id}`);

// fit to canvas when engine stops
Graph.onEngineStop(() => Graph.zoomToFit(400));

