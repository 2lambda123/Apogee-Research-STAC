## Quickstart

If you want to build GraphHopper from source look at the [Developers page](../core/quickstart-from-source.md). 
The following steps are simpler and only need the JRE, a jar file and an OSM file.

 1. Install the latest JRE and get GraphHopper Server as [zip](https://graphhopper.com/public/releases/graphhopper-web-0.5.0-bin.zip) (~9MB)
 2. Unzip it and copy an OSM file into the created directory. For example [berlin-latest.osm.pbf](http://download.geofabrik.de/europe/germany/berlin.html)
 3. Start GraphHopper Maps via: `java -jar *.jar jetty.resourcebase=webapp config=config-example.properties osmreader.osm=berlin-latest.osm.pbf`
 4. After you see 'Started server at HTTP 8989' go to [http://localhost:8989/](http://localhost:8989/) and you should see a map of Berlin. You should be able to click on the map and a route appears.


## Troubleshooting

 * Make sure JRE7 or 8 is installed. If not get Java [here](http://java.com).
 * Regarding step 2:
    * The folder where you execute the java command should contain the following files: berlin-latest.osm.pbf, config-example.properties and `graphhopper-web-[version].jar`
    * The first time you execute this it'll take ~30 seconds (for Berlin), further starts will only load the graph and should be nearly instantaneous. You should see log statements but no exceptions and the last entry should be something like: Started server at HTTP 8989
 * Join the [mailing list](http://graphhopper.com/#developers) and do not hesitate to ask questions!
