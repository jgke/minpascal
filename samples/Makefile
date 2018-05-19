JARFILE=../build/libs/minpascal-fatjar.jar
TARGETS=${JARFILE} arrays assertions caseInsensitive comments hello reading reservedIdentifiers varParameters

# do not remove the .c files
.SECONDARY:
.PHONY: all
all: ${TARGETS}

${JARFILE}:
	(cd ..; ./gradlew fatJar)

% : %.c
	${CC} $< -o $@

%.c : %.mpp
	java -jar ${JARFILE} $< $@
