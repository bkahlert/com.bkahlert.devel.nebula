var com=com||{};com.bkahlert=com.bkahlert||{},com.bkahlert.nebula=com.bkahlert.nebula||{},com.bkahlert.jointjs=com.bkahlert.jointjs||{},function(t){t.extend(com.bkahlert.jointjs,{graph:null,paper:null,initEnabled:!0,start:function(){t("html").addClass("ready");var e=t(window);com.bkahlert.jointjs.graph=new joint.dia.Graph,com.bkahlert.jointjs.paper=new joint.dia.Paper({el:t(".jointjs"),width:e.width(),height:e.height(),model:com.bkahlert.jointjs.graph,elementView:joint.shapes.html.ElementView,linkView:joint.shapes.LinkView}),com.bkahlert.jointjs.setTitle(null),com.bkahlert.jointjs.activateZoomControls(),com.bkahlert.jointjs.activatePanCapability(com.bkahlert.jointjs.paper),com.bkahlert.jointjs.activateLinkCreationCapability(com.bkahlert.jointjs.graph,com.bkahlert.jointjs.paper),com.bkahlert.jointjs.activateLinkTools(),com.bkahlert.jointjs.activateSelections();var o=/[?&]internal=true/.test(location.href);o||(com.bkahlert.jointjs.openDemo(),com.bkahlert.jointjs.autoLayout(),com.bkahlert.jointjs.setTitle("test"))},load:function(t){com.bkahlert.jointjs.graph.clear(),"string"==typeof t&&(t=JSON.parse(t));var e=null;t.title&&(e=t.title,delete t.title);var o=1;t.zoom&&(o=t.zoom,delete t.zoom);var n={x:0,y:0};t.pan&&(n=t.pan,delete t.pan),com.bkahlert.jointjs.graph.fromJSON(t),e&&com.bkahlert.jointjs.setTitle(e),com.bkahlert.jointjs.setZoom(o),com.bkahlert.jointjs.setPan(n.x,n.y);var t=com.bkahlert.jointjs.serialize();return"function"==typeof window.loaded&&window.loaded(t),t},save:function(){var t=com.bkahlert.jointjs.serialize();return"function"==typeof window.save&&window.save(t),t},serialize:function(){var t=JSON.parse(JSON.stringify(com.bkahlert.jointjs.graph));t.title=com.bkahlert.jointjs.getTitle(),t.zoom=com.bkahlert.jointjs.getZoom();var e=com.bkahlert.jointjs.getPan();return t.pan={x:e[0],y:e[1]},JSON.stringify(t)},setEnabled:function(e){e?t("body").removeClass("disabled"):t("body").addClass("disabled")},getTitle:function(){return t(".title").text()},setTitle:function(e){var o=e&&""!=e.trim();t(".title").text(e).css("display",o?"block":"none")},autoLayout:function(){var t=com.bkahlert.jointjs.graph;joint.layout.DirectedGraph.layout(t,{setLinkVertices:!1,nodeSep:.1,edgeSep:.1,rankSep:50,rankDir:"TB"})},onresize:function(){if(com.bkahlert.jointjs.paper){var e=t(window);com.bkahlert.jointjs.paper.setDimensions(e.width(),e.height())}},openDemo:function(){t('<div class="buttons" style="z-index: 9999999"></div>').appendTo("body").css({position:"absolute",top:0,right:0}).append(t("<button>Load</a>").prop("disabled",!0).click(function(){com.bkahlert.jointjs.load(window.saved)})).append(t("<button>Save</a>").click(function(){t(".buttons > :first-child").prop("disabled",!1),window.saved=com.bkahlert.jointjs.save(),console.log(window.saved)})).append(t("<button>Add Node</button>").click(function(){com.bkahlert.jointjs.createNode()})).append(t("<button>Add Link</button>").click(function(){com.bkahlert.jointjs.createLink()})).append(t("<button>Layout</button>").click(function(){com.bkahlert.jointjs.autoLayout()})).append(t("<button>Zoom In</button>").click(function(){com.bkahlert.jointjs.zoomIn()})).append(t("<button>Zoom Out</button>").click(function(){com.bkahlert.jointjs.zoomOut()})).append(t("<button>Get Pan</button>").click(function(){console.log(com.bkahlert.jointjs.getPan())})).append(t("<button>Set Pan</button>").click(function(){com.bkahlert.jointjs.setPan(100,100)})).append(t("<button>Log Nodes/Links</button>").click(function(){console.log(com.bkahlert.jointjs.getNodes()),console.log(com.bkahlert.jointjs.getLinks()),console.log(com.bkahlert.jointjs.getPermanentLinks())})).append(t("<button>Enable</button>").click(function(){com.bkahlert.jointjs.setEnabled(!0)})).append(t("<button>Disable</button>").click(function(){com.bkahlert.jointjs.setEnabled(!1)})).append(t("<button>Custom</button>").click(function(){var t={cells:[{type:"html.Element",position:{x:270,y:142},size:{width:"242",height:"30"},angle:"0",id:"sua://code/-9223372036854775640",content:"",title:"Offensichtliche Usability-Probleme",z:"0",color:"rgb(0, 0, 0)","background-color":"rgba(255, 102, 102, 0.27450980392156865)","border-color":"rgba(255, 48, 48, 0.39215686274509803)",attrs:{}}],title:"New Model",zoom:"1",pan:{x:"0",y:"0"}};console.log(t),com.bkahlert.jointjs.graph.clear(),com.bkahlert.jointjs.graph.fromJSON(t)}));var e=com.bkahlert.jointjs.createNode("sua://test",{position:{x:100,y:300},title:"my box",content:"<ul><li>jkjk</li></ul>"}),o=com.bkahlert.jointjs.createNode("sua://test2",{title:"my box233333"}),n=com.bkahlert.jointjs.createPermanentLink(null,{id:"sua://test"},{id:"sua://test2"}),i=com.bkahlert.jointjs.createNode("sua://test3",{title:"my box233333",position:{x:300,y:300}}),a=com.bkahlert.jointjs.createLink(null,{id:"sua://test3"},{id:"sua://test2"});com.bkahlert.jointjs.setText(n,0,"my_label"),com.bkahlert.jointjs.setText("sua://test2","content","XN dskjd sdkds dskdsdjks dskj "),com.bkahlert.jointjs.setSize(i,300,100),console.log(com.bkahlert.jointjs.getConnectedLinks("sua://test2")),console.log(com.bkahlert.jointjs.getConnectedPermanentLinks("sua://test2")),window.setTimeout(function(){com.bkahlert.jointjs.setPosition(i,500,500)},1e3)},getZoom:function(){return this.paper.getScale().sx},setZoom:function(t){com.bkahlert.jointjs.paper.scale(t)},zoomIn:function(t){com.bkahlert.jointjs.setZoom(1.2*com.bkahlert.jointjs.getZoom())},zoomOut:function(t){com.bkahlert.jointjs.setZoom(.8*com.bkahlert.jointjs.getZoom())},getPan:function(){var t=com.bkahlert.jointjs.paper.getTranslate();return[t.tx,t.ty]},setPan:function(t,e){com.bkahlert.jointjs.paper.translate(t,e)},createNode:function(t,e){var o={id:t};_.extend(o,e);var n=new joint.shapes.html.Element(o);return com.bkahlert.jointjs.graph.addCell(n),n.id},createLink:function(t,e,o){var n={source:e?e:{x:10,y:10},target:o?o:{x:100,y:10},labels:[{position:.5,attrs:{text:{text:""}}}]};t&&_.extend(n,{id:t});var i=new joint.dia.Link(n).attr({".marker-source":{},".marker-target":{d:"M 10 0 L 0 5 L 10 10 z"}}).set("smooth",!0);return com.bkahlert.jointjs.graph.addCell(i),i.id},createPermanentLink:function(t,e,o){var n={className:"test",source:e?e:{x:10,y:10},target:o?o:{x:100,y:10},labels:[{position:.5,attrs:{text:{text:""}}}]};t&&_.extend(n,{id:t});var i=new joint.dia.Link(n).attr({".marker-source":{d:"M 10 0 L 0 5 L 10 10 z"},".marker-target":{},".connection":{"stroke-dasharray":"1,4"}}).set("smooth",!0).set("permanent",!0);return com.bkahlert.jointjs.graph.addCell(i),i.id},removeCell:function(t){var e=com.bkahlert.jointjs.graph.getCell(t);return e?(e.remove(),!0):!1},getNodes:function(){var t=[];return _.each(com.bkahlert.jointjs.graph.getElements(),function(e){t.push(e.id)}),t},getLinks:function(){var t=[];return _.each(com.bkahlert.jointjs.graph.getLinks(),function(e){e.get("permanent")||t.push(e.id)}),t},getPermanentLinks:function(){var t=[];return _.each(com.bkahlert.jointjs.graph.getLinks(),function(e){e.get("permanent")&&t.push(e.id)}),t},getConnectedLinks:function(t){var e=com.bkahlert.jointjs.graph.getCell(t),o=[];return _.each(com.bkahlert.jointjs.graph.getConnectedLinks(e,{}),function(t){t.get("permanent")||o.push(t.id)}),o},getConnectedPermanentLinks:function(t){var e=com.bkahlert.jointjs.graph.getCell(t),o=[];return _.each(com.bkahlert.jointjs.graph.getConnectedLinks(e,{}),function(t){t.get("permanent")&&o.push(t.id)}),o},activateZoomControls:function(){var e=!1;t(document).bind("keyup keydown",function(t){e=t.shiftKey||t.metaKey}),t(document).keydown(function(t){switch(t.which){case 38:case 171:case 107:case 187:com.bkahlert.jointjs.zoomIn(),t.preventDefault(),t.stopPropagation();break;case 40:case 173:case 109:case 189:com.bkahlert.jointjs.zoomOut(),t.preventDefault(),t.stopPropagation()}}),com.bkahlert.jointjs.paper.on("blank:pointerdblclick",function(t,o,n){e?com.bkahlert.jointjs.zoomOut():com.bkahlert.jointjs.zoomIn()})},activatePanCapability:function(e){e.on("blank:pointerdown",function(e){1==e.which&&(com.bkahlert.jointjs.mouseX=e.offsetX,com.bkahlert.jointjs.mouseY=e.offsetY,com.bkahlert.jointjs.mousePan(t(this.viewport).parents("svg"),!0))}),e.on("blank:pointerup",function(){com.bkahlert.jointjs.mousePan(t(this.viewport).parents("svg"),!1)}),t(document).on("mouseleave",".jointjs svg",function(){com.bkahlert.jointjs.mousePan(this,!1)})},mousePanTracker:function(t){var e=t.offsetX-com.bkahlert.jointjs.mouseX,o=t.offsetY-com.bkahlert.jointjs.mouseY,n=com.bkahlert.jointjs.paper.getScale(),i=com.bkahlert.jointjs.paper.getTranslate(),a=i.tx+e/n.sx,r=i.ty+o/n.sy;com.bkahlert.jointjs.paper.translate(a,r),com.bkahlert.jointjs.mouseX=t.offsetX,com.bkahlert.jointjs.mouseY=t.offsetY},mousePan:function(e,o){$svg=t(e),o?($svg.bind("mousemove",com.bkahlert.jointjs.mousePanTracker),$svg.attr("class","grabbing")):($svg.attr("class",""),$svg.unbind("mousemove",com.bkahlert.jointjs.mousePanTracker))},activateLinkCreationCapability:function(e,o){var n=!1;t(document).bind("keyup keydown",function(t){n=t.shiftKey||t.metaKey}),o.on("cell:pointerdblclick",function(t,e,o,i){var a={x:t.model.attributes.position.x,y:t.model.attributes.position.y,w:parseInt(t.model.attributes.size.width),h:parseInt(t.model.attributes.size.height)},r=t.model.id,s={id:r},l={x:a.x+a.w+100,y:i};n&&(l=s,s={x:a.x-100,y:i}),com.bkahlert.jointjs.createLink(null,s,l)})},activateLinkTools:function(){t(document).on("mouseenter",".link[model-id]:not(.permanent)",function(){com.bkahlert.jointjs.showTextChangePopup(t(this).attr("model-id"))}).on("mouseleave",".link[model-id]",function(){com.bkahlert.jointjs.hideTextChangePopup(t(this).attr("model-id"))})},showTextChangePopup:function(e){t(".popover").remove();var o=t("[model-id]").filter(function(){return t(this).attr("model-id")==e});com.bkahlert.jointjs.graph.getCell(e).on("remove",com.bkahlert.jointjs.hideTextChangePopup),o.popover({trigger:"manual",container:"body",title:"",html:!0,content:function(){return t('						<form class="form-inline" role="form">							<div class="form-group">								<label class="sr-only" for="linkTitle">Title</label>								<input type="text" class="form-control input-sm" id="linkTitle" placeholder="Title" value="'+com.bkahlert.jointjs.getText(e,0)+'">							</div>						</form>						').submit(function(){return com.bkahlert.jointjs.hideAndApplyTextChangePopup(o.attr("model-id")),!1})}}).popover("show"),t("#linkTitle").focus()},hideTextChangePopup:function(e){if(e.id)t(".popover").remove();else if(t("#linkTitle").length>0){var o=t("[model-id]").filter(function(){return t(this).attr("model-id")==e});o.popover("destroy")}},hideAndApplyTextChangePopup:function(e){if(t("#linkTitle").length>0){var o=t("[model-id]").filter(function(){return t(this).attr("model-id")==e}),n=t("#linkTitle").val();o.popover("destroy"),com.bkahlert.jointjs.setText(e,0,n)}},activateSelections:function(){var e=t(document);"function"==typeof window.__cellHoveredOver&&e.on("mouseenter","[model-id]",function(){window.__cellHoveredOver(t(this).attr("model-id"))}),"function"==typeof window.__cellHoveredOut&&e.on("mouseleave","[model-id]",function(){window.__cellHoveredOut(t(this).attr("model-id"))})},setText:function(t,e,o){var n=com.bkahlert.jointjs.graph.getCell(t);n instanceof joint.dia.Link?(n.label(e,{attrs:{text:{text:o}}}),"function"==typeof window.__linkTitleChanged&&window.__linkTitleChanged(t,o)):n.set(e,o)},getText:function(t,e){var o=com.bkahlert.jointjs.graph.getCell(t);return o instanceof joint.dia.Link?o.get("labels")[e].attrs.text.text:o.get(e)},setColor:function(t,e){var o=com.bkahlert.jointjs.graph.getCell(t);o.set("color",e)},setBackgroundColor:function(t,e){var o=com.bkahlert.jointjs.graph.getCell(t);o.set("background-color",e)},setBorderColor:function(t,e){var o=com.bkahlert.jointjs.graph.getCell(t);o.set("border-color",e)},setPosition:function(t,e,o){var n=com.bkahlert.jointjs.graph.getCell(t);n.set("position",{x:e,y:o})},setSize:function(t,e,o){var n=com.bkahlert.jointjs.graph.getCell(t);n.set("size",{width:e,height:o})}})}(jQuery),$(window).resize(com.bkahlert.jointjs.onresize),$(document).ready(com.bkahlert.jointjs.start),joint.shapes.LinkView=joint.dia.LinkView.extend({className:function(){var t=["link"];return this.model.get("permanent")&&t.push("permanent"),t.join(" ")},renderTools:function(){if(!this._V.linkTools)return this;var t=$(this._V.linkTools.node).empty(),e=_.template(this.model.get("toolMarkup")||this.model.toolMarkup),o=V(e());return t.append(o.node),this._toolCache=o,this}}),joint.dia.Paper.prototype.getScale=function(){var t=V(this.viewport).attr("transform")||"",e,o=t.match(/scale\((.*)\)/);o&&(e=o[1].split(","));var n=e&&e[0]?parseFloat(e[0]):1,i=e&&e[1]?parseFloat(e[1]):n;return{sx:n,sy:i}},joint.dia.Paper.prototype.oldScale=joint.dia.Paper.prototype.scale,joint.dia.Paper.prototype.scale=function(t,e,o,n){var i=this.getTranslate();this.oldScale(t,e,o,n);var a=this.getScale();$(this.viewport).attr("transform","scale("+a.sx+", "+a.sy+") translate("+i.tx+", "+i.ty+")"),this.$el.find(".html-view").css("transform","scale("+a.sx+", "+a.sy+") translate("+i.tx+"px, "+i.ty+"px)")},joint.dia.Paper.prototype.getTranslate=function(){var t=V(this.viewport).attr("transform")||"",e,o=t.match(/translate\((.*)\)/);o&&(e=o[1].split(","));var n=e&&e[0]?parseFloat(e[0]):0,i=e&&e[1]?parseFloat(e[1]):n;return{tx:n,ty:i}},joint.dia.Paper.prototype.translate=function(t,e){var o=com.bkahlert.jointjs.paper.getScale();$(this.viewport).attr("transform","scale("+o.sx+", "+o.sy+") translate("+t+", "+e+")"),this.$el.find(".html-view").css("transform","scale("+o.sx+", "+o.sy+") translate("+t+"px, "+e+"px)")},joint.shapes.html={},joint.shapes.html.Element=joint.shapes.basic.Rect.extend({defaults:joint.util.deepSupplement({type:"html.Element",position:{x:10,y:10},size:{width:130,height:30},attrs:{rect:{stroke:"none","fill-opacity":0}}},joint.shapes.basic.Rect.prototype.defaults)}),joint.shapes.html.ElementView=joint.dia.ElementView.extend({template:'		<div class="html-element">			<button class="delete hide">x</button>			<h1>Title</h1>			<div class="content"></div>		</div>',initialize:function(){_.bindAll(this,"updateBox"),joint.dia.ElementView.prototype.initialize.apply(this,arguments),this.$box=$(_.template(this.template)()),this.$box.find(".delete").on("click",_.bind(this.model.remove,this.model)),this.model.on("change",this.updateBox,this),com.bkahlert.jointjs.paper.on("scale",this.updateBox,this),this.model.on("remove",this.removeBox,this),this.updateBox()},render:function(){joint.dia.ElementView.prototype.render.apply(this,arguments);var t=this.paper.$el.find(".html-view");return 0==t.length&&(t=$('<div class="html-view"></div>').prependTo(this.paper.$el)),t.prepend(this.$box),this.updateBox(),this},updateBox:function(){var t=this.model.getBBox();this.$box.find("h1").html(this.model.get("title")),this.$box.find(".content").html(this.model.get("content"));var e=this.model.get("color");this.$box.css("color",e?e:"inherit");var o=this.model.get("background-color");this.$box.css("background-color",o?o:"auto");var n=this.model.get("border-color");this.$box.css("border-color",n?n:"auto");var i="rotate("+(this.model.get("angle")||0)+"deg)";this.$box.css({width:t.width,height:t.height,left:t.x,top:t.y,transform:i})},removeBox:function(t){this.$box.remove()}});