default: package

build: package

package:
	@mvn -q package -DskipTests

test:
	@mvn -q test

install:
	@mvn -q install -DskipTests

clean:
	@mvn -q clean

documentation: docs
scaladoc: docs
javadoc: docs

docs:
	@mvn -q scala:doc -DskipTests

# print a variable with `make print-VARIABLE_NAME`
print-%: ; @echo $*=$($*)
