var xmlDoc;

function loadXML(file) {
	var doi = $("#doi").val();
	var projet = $("#selection").val();
	
	 $("button.delete").click();  	// Appuie sur tous boutons '-'
	 $("input").val("");		  	// reset les inputs
	 $("select").val("");		  	// reset les selects
	 $("div.right code").text("");	// efface le xml
	 
	 $("#doi").val(doi);
	 $("#selection").val(projet);
	
//	if(typeof file === 'string'  || file instanceof String){
//		var xhttp = new XMLHttpRequest();
//		xhttp.onreadystatechange = function() {
//			if (this.readyState == 4 && this.status == 200) {
//				// Typical action to be performed when the document is ready:
//				xmlDoc = xhttp.responseXML;
//				
//				update();
//			}
//		};
//		xhttp.open("GET", file, true);
//		xhttp.send();
//	} else {
		xmlDoc = file;
		update();
//	}
}
var attributes;
var tags;
function update() {
	var childs = xmlDoc.documentElement.childNodes;

	for (var i = 0; i < childs.length; i++) {
		if (childs[i].nodeType == 1) {
			switch (childs[i].nodeName) {
			case "identifier":
				updateDOI("identifier", "");
				break;
			case "titles":
				attributes = [ "titleType" ];
				updateGeneral("titles", attributes);
				break;
			case "creators":
				attributes = [ "nameIdentifierScheme", "schemeURI" ];
				updateGeneralSeveral("creators", "creator", attributes);
				break;
			case "publisher":
				updateDOI("publisher", "");
				updateDOI("publicationYear", "");
				break;
			case "resourceType":
				attributes = [ "resourceTypeGeneral" ];
				updateDOI("resourceType", attributes);
				break;
			case "subjects":
				attributes = [ "subjectScheme", "schemeURI", "valueURI" ];
				updateGeneral("subjects", attributes);
				break;
			case "contributors":
				attributes = [ "contributorType", "nameIdentifierScheme",
						"schemeURI" ];
				updateGeneralSeveral("contributors", "contributor", attributes);
				break;
			case "dates":
				attributes = [ "dateType" ];
				updateGeneral("dates", attributes);
				break;
			case "relatedIdentifiers":
				attributes = [ "relatedIdentifierType", "relationType",
						"schemeURI", "schemeType", "relatedMetadataScheme" ];
				updateGeneral("relatedIdentifiers", attributes);
				break;
			case "descriptions":
				attributes = [ "descriptionType" ];
				updateGeneral("descriptions", attributes);
				break;
			case "geoLocations":
				updateGeoLoc("geoLocations");
				break;
			case "language":
				updateDOI("language", "");
				break;
			case "alternateIdentifiers":
				attributes = [ "alternateIdentifierType" ];
				updateGeneral("alternateIdentifiers", attributes);
				break;
			case "sizes":
				updateGeneral("sizes", "");
				break;
			case "formats":
				updateGeneral("formats", "");
				break;
			case "version":
				updateDOI("version", "");
				break;
			case "rightsList":
				attributes = [ "rightsURI" ];
				updateGeneral("rightsList", attributes)
				break;
			case "fundingReferences":
				attributes = [ "funderIdentifierType", "awardURI" ];
				updateGeneralSeveral("fundingReferences", "fundingReference",
						attributes);
				break;
			default:
				break;
			}

		}
	}

}

function updateGeoLoc(mainTag) {
	var x = xmlDoc.getElementsByTagName(mainTag)[0];
	var xlen = x.childNodes.length; // 5 7
	var y = x.firstChild; // text

	var source = $("div[title='" + mainTag + "']");

	// all children element (geoLocation *2)
	var children = [];

	// duplique first
	for (var i = 0; i < xlen; i++) {
		if (y.nodeType == 1) {
			children.push(y);
			if (children.length > 1) {
				source.find("button.add.group").click();
			}
		}
		y = y.nextSibling;
	}

	source = $("div[title='" + children[0].nodeName + "']");
	// fill after
	for (var i = 0; i < children.length; i++) {
		geoLocChild(children[i], source.eq(i));
	}
}
function geoLocChild(node, source) {
	var childrenLength = node.childNodes.length;
	var child = node.firstChild; // text !
	var mySource;

	var testsource;

	var index = 0;
	var duplicate = "";
	for (var i = 0; i < childrenLength; i++) {
		if (child.nodeType == 1) {

			testsource = $("div[title='" + child.nodeName + "']"
					+ " button.add.single-tag");

			if (child.nodeName == duplicate) {
				index++;
				if (index > 3) {
					source.find(testsource).click();
				}
			} else {
				index = 0;
			}
			duplicate = child.nodeName;

			mySource = $(source).find("div[title='" + child.nodeName + "']")
					.eq(index);

			if (child.childNodes.length == 1) {
				mySource.find("[title='" + child.nodeName + "']").val(
						child.childNodes[0].nodeValue);
			} else {
				geoLocChild(child, mySource);
			}
		}
		child = child.nextSibling;
	}

}

function updateGeneralSeveral(mainTag, subTag, attributes) {
	var x = xmlDoc.getElementsByTagName(mainTag)[0];
	var xlen = x.childNodes.length; // 5 7
	var y = x.firstChild; // text

	var source = "div[title='" + mainTag + "']";

	// duplique first
	var nbSubTags = 0;
	for (var i = 0; i < xlen; i++) {
		if (y.nodeType == 1) {
			nbSubTags++;
			if (nbSubTags > 1) {
				$(source + " button.add.group").click();
			}

			if (y.hasAttributes()) {
				for (var j = 0; j < attributes.length; j++) {
					if (y.getAttribute(attributes[j]) != null) {
						$(source).find("div[title='" + y.nodeName + "']").eq(
								nbSubTags - 1).find(
								"[title='" + attributes[j] + "']").val(
								y.getAttribute(attributes[j]));

					}
				}
			}

		}
		y = y.nextSibling;
	}

	// fill after
	for (var i = 0; i < nbSubTags; i++) {
		children(i, subTag, attributes);
	}

}
function children(nbSubTags, subTag, attributes) {
	var x = xmlDoc.getElementsByTagName(subTag)[nbSubTags];
	var xlen = x.childNodes.length;
	var y = x.firstChild;

	var source = "div[title='" + subTag + "']";

	var index = 0;
	var duplicate = "";
	for (var i = 0; i < xlen; i++) {
		if (y.nodeType == 1) {
			// check if an element is repeated, if true, simulate a click on
			// button '+'
			if (y.nodeName == duplicate) {
				$(source).find(
						"div[title='" + y.nodeName + "']"
								+ " button.add.single-tag").eq(nbSubTags)
						.click();
				index++;
			} else {
				index = 0;
			}
			duplicate = y.nodeName;

			$(source).eq(nbSubTags).find("div[title='" + y.nodeName + "']")
					.find("[title='" + y.nodeName + "']").eq(index).val(
							y.childNodes[0].nodeValue);

			// write
			if (y.hasAttributes()) {
				for (var j = 0; j < attributes.length; j++) {
					if (y.getAttribute(attributes[j]) != null) {
						$(source).eq(nbSubTags).find(
								"[title='" + attributes[j] + "']").eq(index)
								.val(y.getAttribute(attributes[j]));

					}
				}
			}

		}
		y = y.nextSibling;
	}
}

function updateDOI(mainTag, attributes) {
	var x = xmlDoc.getElementsByTagName(mainTag)[0];
	var xlen = x.childNodes.length;
	var y = x.firstChild;

	var source = "div[title='" + mainTag + "']";

	if (xlen == 1) {
		$(source).find("[title='" + x.nodeName + "']").val(y.nodeValue);
		if (x.hasAttributes()) {
			for (var j = 0; j < attributes.length; j++) {
				if (x.getAttribute(attributes[j]) != null) {
					$(source).find("[title='" + attributes[j] + "']").val(
							x.getAttribute(attributes[j]));

				}
			}
		}

	}
}

function updateGeneral(mainTag, attributes) {
	var x = xmlDoc.getElementsByTagName(mainTag)[0];
	var xlen = x.childNodes.length; // 5 7
	var y = x.firstChild; // text

	var source = "div[title='" + mainTag + "']";

	for (var i = 0; i < (xlen - 3) / 2; i++) {
		$(source + " button.add.group").click();
	}

	var index = 0;
	for (var i = 0; i < xlen; i++) { // 5
		if (y.nodeType == 1) {
			$(source).find("div[title='" + y.nodeName + "']").find(
					"[title='" + y.nodeName + "']").eq(index).val(
					y.childNodes[0].nodeValue);
			for (var j = 0; j < attributes.length; j++) {

				if (y.getAttribute(attributes[j]) != null) {
					$(source + " [title='" + attributes[j] + "']").eq(index)
							.val(y.getAttribute(attributes[j]));
				}
			}
			index++;
		}
		y = y.nextSibling;

	}
}