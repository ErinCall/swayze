default: package

build: package

package:
	@mvn -q clean package -Dmaven.test.skip=true

test:
	@mvn -q clean test

install:
	@mvn -q clean install -Dmaven.test.skip=true

clean:
	@mvn -q clean

documentation: docs
scaladoc: docs
javadoc: docs

docs:
	@mvn -q clean scala:doc -Dmaven.test.skip=true
