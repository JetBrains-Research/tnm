const elem = document.getElementById('3d-graph');

const Graph = ForceGraph3D()(elem)
    .graphData(data)
    .linkWidth(link => link.value)
    .linkColor(link => link.color)
    .linkCurvature(0.25)
    .linkOpacity(1)
    .nodeOpacity(1)
    .nodeAutoColorBy('color')
    .nodeLabel(node => `${node.id} `);

// fit to canvas when engine stops
Graph.onEngineStop(() => Graph.zoomToFit(400));

