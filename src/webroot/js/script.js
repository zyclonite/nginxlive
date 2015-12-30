function prepareQueryData(query) {
    var queryObject = URI(query).search(true);
    var data = {};
    $.each(queryObject, function(key, val) {
        if(val.length > 0) {
            var highlow = val.substring(0,1);
            if(highlow === '<' || highlow === '>') {
                var operator = (highlow === '<')?'$lte':'$gte';
                val = val.substring(1);
                if(!isNaN(val)) {
                    data[key] = {};
                    data[key][operator] = Number(val);
                }
            } else {
                if(isNaN(val)) {
                    if(val.substring(val.length-1, val.length) === '*') {
                        val = val.substring(0, val.length-1);
                        data[key] = {};
                        data[key]['$regex'] = '^'+val+'.*';
                        data[key]['$options'] = 'i';
                    } else {
                        data[key] = val;
                    }
                } else {
                    data[key] = Number(val);
                }
            }
        }
    });
    return data;
}

function get(year, month, query) {
    var queryData = prepareQueryData(query);
    $.ajax({
        url: './db/hits/'+year+'/'+month,
        data: {
            query: btoa(JSON.stringify(queryData))
        },
        type: 'GET',
        dataType: 'json',
        success: function(result) {
            var table = $('#table');
            $.each(result, function(i, row) {
                table.append('<tr><td>'+row._id.day+'</td><td>'+row._id.host+'</td><td>'+row.hits+'</td><td>'+bytesToSize(row.bytesReceived)+'</td><td>'+bytesToSize(row.bytesSent)+'</td></tr>');
            });
        }
    });
}

function bytesToSize(bytes) {
   var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
   if (bytes == 0) return '0 Byte';
   var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
   return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
}

var maxlines = 100;
var wsocket = function(loc, livelist) {
    sock = SockJS(loc.protocol+'//'+loc.hostname+(loc.port===undefined?'':':'+loc.port)+loc.pathname.replace(/[^/]*$/,'')+'io');
    sock.onopen = function() {
        var subscribeAccess = new Object();
        subscribeAccess.name = "access";
        subscribeAccess.type = "access";
        sock.send(JSON.stringify(subscribeAccess));
    };
    sock.onmessage = function(e) {
        var logline = JSON.parse(e.data);
        var logdate = new Date(logline.time.$date);
        var line = logdate.toTimeString()+' '+logline.remoteAddr+' -'+logline.requestMethod+'-> '+logline.scheme+'://'+logline.host+logline.requestURI+' status: '+logline.status;
        $('<div class="logline '+((logline.status >= 300)?((logline.status >= 500)?'uk-text-danger':'uk-text-warning'):'uk-text-success')+'" />').prependTo(livelist).html(line);
        livelist.find('.logline:gt('+maxlines+')').remove();
    };
    sock.onclose = function() {
        setTimeout(function () {
                   wsocket(loc, livelist);
               }, 2000);
    };
};
