package visualisation

import kotlinx.html.*
import kotlinx.html.stream.appendHTML

object UtilVisualisation {
    const val defaultStyleHTML = """
        #mynetwork {
            width: 100%;
            height: 1080px;
            background-color: #222222;
            border: 1px solid lightgray;
            position: relative;
            float: left;
        }

        
        #loadingBar {
            position:absolute;
            top:0px;
            left:0px;
            width: 100%;
            height: 1080px;
            background-color:rgba(200,200,200,0.8);
            -webkit-transition: all 0.5s ease;
            -moz-transition: all 0.5s ease;
            -ms-transition: all 0.5s ease;
            -o-transition: all 0.5s ease;
            transition: all 0.5s ease;
            opacity:1;
        }

        #bar {
            position:absolute;
            top:0px;
            left:0px;
            width:20px;
            height:20px;
            margin:auto auto auto auto;
            border-radius:11px;
            border:2px solid rgba(30,30,30,0.05);
            background: rgb(0, 173, 246); /* Old browsers */
            box-shadow: 2px 0px 4px rgba(0,0,0,0.4);
        }

        #border {
            position:absolute;
            top:10px;
            left:10px;
            width:500px;
            height:23px;
            margin:auto auto auto auto;
            box-shadow: 0px 0px 4px rgba(0,0,0,0.2);
            border-radius:10px;
        }

        #text {
            position:absolute;
            top:8px;
            left:530px;
            width:30px;
            height:50px;
            margin:auto auto auto auto;
            font-size:22px;
            color: #000000;
        }

        div.outerBorder {
            position:relative;
            top:400px;
            width:600px;
            height:44px;
            margin:auto auto auto auto;
            border:8px solid rgba(0,0,0,0.1);
            background: rgb(252,252,252); /* Old browsers */
            background: -moz-linear-gradient(top,  rgba(252,252,252,1) 0%, rgba(237,237,237,1) 100%); /* FF3.6+ */
            background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(252,252,252,1)), color-stop(100%,rgba(237,237,237,1))); /* Chrome,Safari4+ */
            background: -webkit-linear-gradient(top,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* Chrome10+,Safari5.1+ */
            background: -o-linear-gradient(top,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* Opera 11.10+ */
            background: -ms-linear-gradient(top,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* IE10+ */
            background: linear-gradient(to bottom,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* W3C */
            filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#fcfcfc', endColorstr='#ededed',GradientType=0 ); /* IE6-9 */
            border-radius:72px;
            box-shadow: 0px 0px 10px rgba(0,0,0,0.2);
        }
        """
    const val defaultDrawFunctionName = "drawGraph"

    fun edgesToVis(edges: Set<GraphHTML.EdgeInfo>): String {
        return "edges = new vis.DataSet([ ${edges.joinToString(", ") { it.generateVis() }} ]);"
    }

    fun nodesToVis(nodes: Set<GraphHTML.NodeInfo>): String {
        return "nodes = new vis.DataSet([ ${nodes.joinToString(", ") { it.generateVis() }} ]);"
    }

    fun headH1GraphName(graphName: String): String {
        return """
        <center>
        <h1>$graphName</h1>
        </center>
        """.trimIndent()
    }

    fun defaultHead(graphName: String, title: String = graphName): String {
        return buildString {
            appendHTML().head {
                link {
                    rel = "stylesheet"
                    href = "https://cdnjs.cloudflare.com/ajax/libs/vis/4.16.1/vis.css"
                    type = "text/css"
                }
                script {
                    type = "text/javascript"
                    src = "https://cdnjs.cloudflare.com/ajax/libs/vis/4.16.1/vis-network.min.js"
                }
                style {
                    unsafe {
                        raw(defaultStyleHTML)
                    }
                }
                title(title)

                unsafe {
                    raw(headH1GraphName(graphName))
                }
            }
        }
    }

    fun defaultScriptVis(nodes: Set<GraphHTML.NodeInfo>, edges: Set<GraphHTML.EdgeInfo>): String {
        return """
            var edges;
            var nodes;
            var network; 
            var container;
            var options, data;
                
            function $defaultDrawFunctionName() {
                var container = document.getElementById('mynetwork');
                ${nodesToVis(nodes)}
                ${edgesToVis(edges)}
                data = {nodes: nodes, edges: edges};
                var options = {
                    "configure": {
                        "enabled": false
                    },
                    "edges": {
                        "color": {
                            "inherit": true
                        },
                        "smooth": {
                            "enabled": false,
                            "type": "continuous"
                        }
                    },
                    "interaction": {
                        "dragNodes": true,
                        "hideEdgesOnDrag": false,
                        "hideNodesOnDrag": false
                    },
                    "physics": {
                        "barnesHut": {
                            "avoidOverlap": 0,
                            "centralGravity": 0.3,
                            "damping": 0.09,
                            "gravitationalConstant": -80000,
                            "springConstant": 0.001,
                            "springLength": 250
                        },
                        "enabled": true,
                        "stabilization": {
                            "enabled": true,
                            "fit": true,
                            "iterations": 1000,
                            "onlyDynamicEdges": false,
                            "updateInterval": 50
                        }
                    }
                }; 
                network = new vis.Network(container, data, options);
                network.on("stabilizationProgress", function(params) {
                    document.getElementById('loadingBar').removeAttribute("style");
                    var maxWidth = 496;
                    var minWidth = 20;
                    var widthFactor = params.iterations/params.total;
                    var width = Math.max(minWidth,maxWidth * widthFactor);
        
                    document.getElementById('bar').style.width = width + 'px';
                    document.getElementById('text').innerHTML = Math.round(widthFactor*100) + '%';
                });
                network.once("stabilizationIterationsDone", function() {
                    document.getElementById('text').innerHTML = '100%';
                    document.getElementById('bar').style.width = '496px';
                    document.getElementById('loadingBar').style.opacity = 0;
                    setTimeout(function () {document.getElementById('loadingBar').style.display = 'none';}, 500);
                });
        
                return network;
            }
            drawGraph();
        """
    }

    fun defaultBody(
        nodes: Set<GraphHTML.NodeInfo>, edges: Set<GraphHTML.EdgeInfo>,
        script: String = defaultScriptVis(nodes, edges)
    ): String {
        return buildString {
            appendHTML().body {
                div {
                    id = "mynetwork"
                }

                div {
                    id = "loadingBar"
                    div {
                        classes = setOf("outerBorder")
                        div {
                            id = "text"
                            +"0%"
                        }

                        div {
                            id = "border"
                            div {
                                id = "bar"
                            }
                        }
                    }
                }

                script {
                    type = "text/javascript"
                    unsafe {
                        raw(script)
                    }
                }
            }
        }
    }

}
