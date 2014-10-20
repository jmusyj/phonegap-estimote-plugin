phonegap-estimote-plugin
========================

PhoneGap plugin to interface with Estimote SDK

Installation
------------
Check out PhoneGap CLI [docs](http://docs.phonegap.com/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface) before starting out.

To install this plugin on 3.0.0, use the phonegap CLI.

```
phonegap local plugin add https://github.com/jmusyj/phonegap-estimote-plugin.git
```

Remember to build the project afterwards.

Usage
-----

```
if (window.estimote) {
  window.estimote.startRanging(
    // success callback
    function(result) {
      console.info(result.name);
      console.info(result.address);
      console.info(result.proximityUUID);
      console.info(result.major);
      console.info(result.minor);
      console.info(result.rssi);
      console.info(result.measuredPower);
    },
    // error callback
    function(result) {
      console.error(result.message);
      console.error(result.code);
    }
  );
} else {
  console.error("No window.estimote defined. Check plugin installation.");
}
```

License
-------
TODO
