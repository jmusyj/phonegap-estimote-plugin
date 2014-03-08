var exec = require('cordova/exec');

/**
 * Create a new instance of Estimote(Plugin).
 * 
 * @class       Estimote
 * @classdesc   EstimotePlugin for cordova 3.0.0+ (PhoneGap).
 */
var Estimote = function() 
{
    this.platforms = [ "android" ];
};

/**
 * Start the device discovery process.
 *
 * @memberOf Estimote
 * 
 * @param  {Estimote~onDeviceDiscovered}   onDeviceDiscovered      Invoked when a device is found.
 * @param  {Estimote~onSuccess}            onDiscoveryFinished     Invoked when discovery finishes succesfully.
 * @param  {Estimote~onError}              onError                 Invoked if there is an error, or the discovery finishes prematurely.
 */
Estimote.prototype.startRanging = function(onDeviceDiscovered, onRangingFinished, onError) 
{
    var timeout = function()
    {
        onError({ code: 9001, message: "Request timed out" });
    }

    this.timeout = setTimeout(timeout, 15000);

    var self = this;
    exec(function(result)
    {
        if(result === false)
        {
            clearTimeout(self.timeout);
            onRangingFinished();
        }
        else
        {
            onDeviceDiscovered(result);
        }
    }, 
    function(error)
    {   
        clearTimeout(self.timeout);
        onError(error);
    }, 
    "Estimote", "startRanging", []);
}

var estimote   = new Estimote();
module.exports  = estimote;
