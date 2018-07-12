run:
	./gradlew shadowJar && \
	spark-submit --verbose \
	--class com.github.geoheil.geomesaGeospark.Foo \
	--master 'local[*]' \
	--driver-memory 8G \
	build/libs/geomesaGeospark-all.jar

replSparkShell:
	./gradlew shadowJar && \
	spark-shell --master 'local[2]' \
	--jars build/libs/geomesaGeospark-all.jar