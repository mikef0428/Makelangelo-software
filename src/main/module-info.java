module com.marginallyclever.makelangelo {
	requires transitive java.desktop;
	requires java.prefs;
	requires java.logging;
	requires org.apache.commons.io;
	requires org.json;
	requires org.jetbrains.annotations;
	requires org.slf4j;

	requires transitive jrpicam;
	requires transitive jogamp.fat;
	requires transitive kabeja;
	requires transitive jssc;
	requires transitive vecmath;
	requires transitive batik.all;
	requires transitive xml.apis.ext;
	
	exports com.marginallyclever.makelangelo;
	exports com.marginallyclever.makelangelo.select;
	exports com.marginallyclever.makelangelo.turtle;
	exports com.marginallyclever.convenience;
}