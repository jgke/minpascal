TARGETS=arrays assertions caseInsensitive comments hello reading reservedIdentifiers varParameters
JARFILE=../build/libs/minpascal-fatjar.jar

# do not remove the .c files
.SECONDARY:
.PHONY: all
all: ${TARGETS}

% : %.c
	${CC} $< -o $@

%.c : %.mpp
	java -jar ${JARFILE} $< $@
