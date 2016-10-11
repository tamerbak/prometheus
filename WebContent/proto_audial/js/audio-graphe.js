var paper = Raphael("canvas", 900, 500);
var right_ear;
var right_ear_second;
var dots =[];
var dots2 = [];
var abscisses = [67, 210, 351, 422,494,564,636,707,779,850,920];
var right_ear_path=[];
var right_ear_path_type="circle";

$("#canvas").click(function(e){
	var currentX  = e.offsetX;

	for(ab = 0; ab < abscisses.length; ab++) {
		if(Math.abs(abscisses[ab]-currentX) < 5) {
			break;
		}
	}
	var cx = abscisses[ab];
	var cy = e.offsetY;
	if(ab < abscisses.length) {
		if(right_ear_path_type == "circle") {
			if(dots[ab] != null) {
				dots[ab].shape.remove();
			}
			var c = paper.circle(cx, cy,3).attr({fill:"#f00", "stroke":"#f00", "stroke-width": 0});
			dots[ab] = {x:cx, y:cy, shape:c};
			// if(dots.length == 1) {
			// 	stPath = "M"+dots[0].x+","+dots[0].y;
			// 	right_ear = paper.path(stPath).attr({"stroke":"#f00", "stroke-width": 1});
			// } else {
				drawPath();
			// }
		} else {
			if(dots2[ab] != null) { 
				dots2[ab].shape.remove();
			}
			var r = paper.rect(cx-3, cy-3,6,6).attr({fill:"#f00", "stroke":"#f00", "stroke-width": 0});
			dots2[ab] = {x:cx, y:cy, shape:r};
			drawPath();
		}
	}
});

drawPath = function(){
	if(right_ear_path_type == "circle") {
		var keys = Object.keys(dots);
		stPath = "M"+dots[keys[0]].x+","+dots[keys[0]].y;
		for(i = 1; i<keys.length; i++){
			if(dots[keys[i]]!=null) {
				stPath += "L"+dots[keys[i]].x+","+dots[keys[i]].y;
			}
		}
		if(right_ear==null){
			right_ear = paper.path(stPath).attr({"stroke":"#f00", "stroke-width": 1});
		} else {
			right_ear.attr("path", stPath); 
		}
	} else {
		var keys = Object.keys(dots2);
		stPath = "M"+dots2[keys[0]].x+","+dots2[keys[0]].y;
		for(i = 1; i<keys.length; i++){
			if(dots2[keys[i]]!=null) {
				stPath += "L"+dots2[keys[i]].x+","+dots2[keys[i]].y;
			}
		}
		if(right_ear_second==null){
			right_ear_second = paper.path(stPath).attr({"stroke":"#f00", "stroke-width": 1});
		} else {
			right_ear_second.attr("path", stPath); 
		}
	}
}

$(".type-graphe").click(function(){
	right_ear_path_type = $(this).data("type");
	$(".type-graphe").attr("style","color:#000");
	$(this).attr("style","color:red");
});
