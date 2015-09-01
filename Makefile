ifdef QUIET
    QUIETOPT := "-q"
endif

default: package

build: package

package:
	@mvn ${QUIETOPT} package -DskipTests

test:
	@mvn ${QUIETOPT} test

install:
	@mvn ${QUIETOPT} install -DskipTests

clean:
	@mvn ${QUIETOPT} clean

documentation: docs
scaladoc: docs
javadoc: docs

docs:
	@mvn ${QUIETOPT} scala:doc -DskipTests

# print a variable with `make print-VARIABLE_NAME`
print-%: ; @echo $*=$($*)
