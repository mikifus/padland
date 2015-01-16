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

    /*
    var iframe = $("#pad iframe").contents();
    $(iframe).load(function(){
            $(this).show();
            iframe.find("#chatbox").hide();
            console.log('laod the iframe');
        });
        */
}