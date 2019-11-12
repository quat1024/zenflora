ZenFlora
=======

You can make flowers with ZenScript. How dandy. Put documentation here later.

### Preview

This isn't documentation, just writing down what I think the api should look like.

```zenscript
var myFlower = (something here)

myFlower.onUpdate = function(me as IFlower, sup as OnUpdateSuper) {
  sup.onUpdate();
  
  var world = me.world;
};

```