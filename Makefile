default: package

build: package

package:
	@mvn -q clean package -DskipTests

test:
	@mvn -q clean test

install:
	@mvn -q clean install -DskipTests

clean:
	@mvn -q clean

documentation: docs
scaladoc: docs
javadoc: docs

docs:
	@mvn -q clean scala:doc -DskipTests

# print a variable with `make print-VARIABLE_NAME`
print-%: ; @echo $*=$($*)