<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<!DOCTYPE html>
<html>
  <!-- start index.jsp -->
  <head>
<link href="../../grouperExternal/public/assets/dojo/dijit/themes/claro/claro.css" rel="stylesheet" type="text/css" />
<script src="../../grouperExternal/public/assets/dojo/dojo/dojo.js"></script>

<script type="text/javascript" >
    dojo.require("dojo/ready");
    dojo.require("dojo/parser");
    dojo.require("dijit.Tree");
    dojo.require("dojo.store.Memory");
    dojo.require("dijit/tree/ObjectStoreModel");

    
</script>

  </head>
  <body class="claro">
                            

<div id="tree"></div>

<br /><br />

<div class="info"></div>
<br /><br />
    <script>
    var myStore = new dojo.store.Memory({
      data: [
            { id: 'world', name:'The earth', type:'planet', population: '6 billion'},
            { id: 'AF', name:'Africa', type:'continent', population:'900 million', area: '30,221,532 sq km',
                    timezone: '-1 UTC to +4 UTC', parent: 'world'},
                { id: 'EG', name:'Egypt', type:'country', parent: 'AF' },
                { id: 'KE', name:'Kenya', type:'country', parent: 'AF' },
                    { id: 'Nairobi', name:'Nairobi', type:'city', parent: 'KE' },
                    { id: 'Mombasa', name:'Mombasa', type:'city', parent: 'KE' },
                { id: 'SD', name:'Sudan', type:'country', parent: 'AF' },
                    { id: 'Khartoum', name:'Khartoum', type:'city', parent: 'SD' },
            { id: 'AS', name:'Asia', type:'continent', parent: 'world' },
                { id: 'CN', name:'China', type:'country', parent: 'AS' },
                { id: 'IN', name:'India', type:'country', parent: 'AS' },
                { id: 'RU', name:'Russia', type:'country', parent: 'AS' },
                { id: 'MN', name:'Mongolia', type:'country', parent: 'AS' },
            { id: 'OC', name:'Oceania', type:'continent', population:'21 million', parent: 'world'},
            { id: 'EU', name:'Europe', type:'continent', parent: 'world' },
                { id: 'DE', name:'Germany', type:'country', parent: 'EU' },
                { id: 'FR', name:'France', type:'country', parent: 'EU' },
                { id: 'ES', name:'Spain', type:'country', parent: 'EU' },
                { id: 'IT', name:'Italy', type:'country', parent: 'EU' },
            { id: 'NA', name:'North America', type:'continent', parent: 'world' },
            { id: 'SA', name:'South America', type:'continent', parent: 'world' }
        ],
        getChildren: function(object){
          return this.query({parent: object.id});
        }
    });
 
 // Create the model
    var myModel = new dijit.tree.ObjectStoreModel({
        store: myStore,
        query: {id: 'world'}
    });

    tree = new dijit.Tree({
      model: myModel
    }, "tree"); // make sure you have a target HTML element with this id

    tree.startup();

    
    dojo.query(".info").attr("innerHTML", dojo.version);

    </script>

  </body>
</html>