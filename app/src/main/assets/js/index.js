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
        'borderStyle' : "double",
    });

    var iframe = $("#pad iframe");
    $(iframe).load(function(){
            $(this).show();
            //webviewScriptAPI.unsetLoading();
            console.log('iframe loaded');
        });

}
function PadViewResize( w, h ){
    $("#pad").width(w).height(h);
    console.log("Pad view resize triggered: " + w + ", " + h )
}