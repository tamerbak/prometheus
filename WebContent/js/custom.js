function clickMiniature(imgMin){
	//recuperer les tags img
	var imgs = document.getElementsByTagName('img');
	//iterer sur ces tag 
	for ( var i = 0; i < imgs.length; i++) {
		//pour voir ceux qui ont dans un id comme imageMiniature
		if(imgs[i].id.indexOf("imageMiniature")){
			//enlever le border a ceux qui ont l'id comme imageMiniature
			imgs[i].style.borderWidth="0px";
		}
	}
	//mettre le border pour l'image cliquee
	imgMin.style.borderColor="rgb(150, 150, 150)";
	imgMin.style.borderWidth="9px";
	imgMin.style.borderStyle="double";
}

//Change stylesheets files
function changeCSS(cssFileColor, cssFileStyle, cssFileAdmin, cssFileLayout) {
	// var ref = document.getElementById('reset');

	// modifier le fichier color.css du thème Developr
	var sheetToBeRemoved = document.getElementById('color');
	if (sheetToBeRemoved != null) {
		var newlink = document.createElement("link");
		newlink.setAttribute("rel", "stylesheet");
		newlink.setAttribute("type", "text/css");
		newlink.setAttribute("href", cssFileColor);
		newlink.setAttribute("id", 'color');
		document.getElementsByTagName('head')[0].replaceChild(newlink,
				sheetToBeRemoved);
	}

	// modifier le fichier style.css du thème Developr
	sheetToBeRemoved = document.getElementById('style');
	if (sheetToBeRemoved != null) {
		newlink = document.createElement("link");
		newlink.setAttribute("rel", "stylesheet");
		newlink.setAttribute("type", "text/css");
		newlink.setAttribute("href", cssFileStyle);
		newlink.setAttribute("id", 'style');
		document.getElementsByTagName('head')[0].replaceChild(newlink,
				sheetToBeRemoved);
	}
	// modifier le fichier style.css du thème bleu de Jakjoud

	sheetToBeRemoved = document.getElementById('admin');
	if (sheetToBeRemoved != null) {
		newlink = document.createElement("link");
		newlink.setAttribute("rel", "stylesheet");
		newlink.setAttribute("type", "text/css");
		newlink.setAttribute("href", cssFileAdmin);
		newlink.setAttribute("id", 'admin');
		document.getElementsByTagName('head')[0].replaceChild(newlink,
				sheetToBeRemoved);
	}
	
	// modifier le fichier layout.css du thème rouge

	sheetToBeRemoved = document.getElementById('layout');
	if (sheetToBeRemoved != null) {
		newlink = document.createElement("link");
		newlink.setAttribute("rel", "stylesheet");
		newlink.setAttribute("type", "text/css");
		newlink.setAttribute("href", cssFileLayout);
		newlink.setAttribute("id", 'layout');
		document.getElementsByTagName('head')[0].replaceChild(newlink,
				sheetToBeRemoved);
	}
}