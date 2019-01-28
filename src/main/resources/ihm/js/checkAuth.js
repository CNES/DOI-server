var path = window.location.pathname;
var page = path.split("/").pop();

if(sessionStorage.getItem("user") == null){
	window.location.href = ".";
}
if(sessionStorage.getItem("token") == null){
	window.location.href = ".";
}

if(page == "administration.html" || page == "administration"){
	if(sessionStorage.getItem("admin") == "false"){
		window.location.href = ".";
	}
} else {
	if(sessionStorage.getItem("projects") == null){
		window.location.href = ".";
	}
}