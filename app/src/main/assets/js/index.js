function start(url, username, color){
    var name = url.slice(url.lastIndexOf('/')+1);
    var host = url.slice(0,url.lastIndexOf('/')-2);
    var baseUrl = url.replace(name,'').replace(host,'');

    $('#pad').pad({
        'host' : host,
        'baseUrl' : baseUrl,
        'padId': name,
        'userName' : username,
        'userColor' : color || "#555",
        'showControls' : "true",
        'showChat' : "false",
        'borderStyle' : "double"
    });
    console.log('pad called');

    var iframe = $("iframe");

    $(iframe).on("load", function(){
            console.log('iframe loaded');
            webviewScriptAPI.onIframeLoaded();
        });

}
function PadViewResize( w, h ){
    $("#pad").width(w).height(h);
    console.log("Pad view resize triggered: " + w + ", " + h );
}