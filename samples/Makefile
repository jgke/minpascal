JARFILE=../build/libs/minpascal-fatjar.jar
TARGETS=arrays assertions caseInsensitive comments hello reading reservedIdentifiers varParameters

# do not remove the .c files
.SECONDARY:
.PHONY: all
all: ${JARFILE} ${TARGETS}

${JARFILE}:
	(cd ..; ./gradlew fatJar)

% : %.c
	${CC} $< -o $@

%.c : %.mpp
	java -jar ${JARFILE} $< $@

.PHONY: run
run: all
	@for i in ${TARGETS}; do \
	    echo "Source code: "; \
	    echo ""; \
	    cat "$$i.mpp"; \
	    echo ""; \
	    read -p "Running $$i, press enter to continue"; \
	    echo "output:"; \
	    ./$$i; \
	    echo ""; \
	done
