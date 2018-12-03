# VARIABLES

# DATADIR
# Directory that contains Marc21 records.
#
# Format: Marc21
# Compression: gzip
# Suffix: mrc.gz or marc21.gz
#
DATADIR = ./testdata

# TARGETDIR
# Directory for build results
#
TARGETDIR = ./target

# THREADS
# Number of parallel threads
#
THREADS = 1

# EXPORTLIMIT
# Limit the records per aggregated for make target 'aggregate'.
# A component that exceeds this limit, will be skipped during the aggregation step.
# Default: -1 (unlimited)
EXPORTLIMIT = -1

# RULE
# Name of the rule set that is used for key construction.
# Choose: [wrk]
#
RULE := $(filter ${RULENAME}, wrk)
RULE := $(or $(RULE), wrk)

# JVM_OPTS
# JVM setting for default java tasks
#
JVM_OPTS = -Xmx2g -Dfile.encoding=UTF-8 -Dorg.slf4j.simpleLogger.defaultLogLevel=off -Dorg.slf4j.simpleLogger.logFile=System.err 

# JVM_OPTS_HEAVY
# JVM settings for computational intensive java tasks
#
JVM_OPTS_HEAVY = -Xmx12g -XX:+UseCompressedOops -XX:+UseG1GC -Dfile.encoding=UTF-8 -Dorg.slf4j.simpleLogger.defaultLogLevel=off -Dorg.slf4j.simpleLogger.logFile=System.err

# GROOVYJAR
# Jar that provides a standalone environment for a collection of groovy scripts.
# Contains groovy, groovy scripts and their depenendecies.
#
GROOVYJAR=$(shell ls lib/*.jar | head -1)

# groovy
# Prefix for java to run the included groovy interpreter
#
run-groovy=-cp $(GROOVYJAR) groovy.ui.GroovyMain

## Functions

# script
# Resolves path to Groovy Script by name
#
# Read script from within jar: script=jar:file:$(WORKFLOWSJAR)!/$(1).groovy
script=./scripts/$(1)

# false-if-empty
# Returns 1, if the file is present and has no content. 0 otherwise.
#
# Prosa: Is file present AND has no content, PRINT something and return 1
false-if-empty=if [ -f $(1) ] && [ $$(gunzip -c $(1) | head -c1 | wc -c) -eq 0 ] && echo "File $(1) is empty."; then false; fi;

help:
	@echo "all         : Compute all possible targets."
	@echo "keys        : Compute matchkeys for records in DATADIR (Algorithm: $(RULE))."
	@echo "statistic   : Compute the frequency distribution for each key type."
	@echo "cluster     : Compute cluster that group records that are connected by their keys through bfs."
	@echo "idn-list    : Compute a idn list with all idns that form a cluster per line."
	@echo "bundle      : A xml dump that contains clustered record (Format: Bundle-XML)"
	@echo "aggregate   : A xml dump that aggregates clustered record (Format: MARCXML)"
	@echo
	@echo "DATADIR     : Directory with gzip compressed input data in Marc21 (Default: $(DATADIR))."
	@echo "RULENAME    : Behaviour for key generation. Choose: [wrk] (Default: $(RULE))."
	@echo "TARGETDIR   : Directory that contains build results (Default: $(TARGETDIR))."
	@echo "THREADS     : Number of parallel threads (Default: $(THREADS))."
	@echo "EXPORTLIMIT : Maximum size for aggregated record (Default: $(EXPORTLIMIT)). Affects only build step 'aggregate'."


$(TARGETDIR):
	@echo
	@echo
	@echo "[Creating target folder $(TARGETDIR)]"
	@echo

	mkdir -p $(TARGETDIR)

.PHONY: clean
clean:
	if [ -d "$(TARGETDIR)" ]; then rm -rf $(TARGETDIR); fi

.PHONY: all
all: $(TARGETDIR) keys keys-statistic cluster cluster-idn-list cluster-size-frequency bundle aggregate


.PHONY: keys
keys: $(TARGETDIR) $(TARGETDIR)/keys.csv.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/keys.csv.gz:
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	find $(DATADIR) -type f \
	| grep "\.gz$$" \
	| xargs gunzip -c \
	| java $(JVM_OPTS) $(run-groovy) scripts/WorkflowKeys.groovy \
	 --input - \
	 --morph metamorph/ingestMarc21.xml \
	 --morph metamorph/$(RULE).xml \
	 --threads $(THREADS) \
	| gzip\
	> $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)


.PHONY: statistic
statisitc: keys-statistic

.PHONY: keys-statistic
keys-statistic: $(TARGETDIR) $(TARGETDIR)/keys-statistic.csv.gz

$(TARGETDIR)/keys-statistic.csv.gz:
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	find $(DATADIR) -type f \
	| grep "\.gz$$" \
	| xargs gunzip -c \
	| java $(run-groovy) scripts/WorkflowKeysStatistic.groovy \
	 --input - \
	 --morph metamorph/ingestMarc21.xml \
	 --morph metamorph/$(RULE).xml \
	 --xsl xslt/keyStatistic.xsl \
	 --threads $(THREADS) \
	| gzip\
	> $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)


.PHONY: cluster
cluster: $(TARGETDIR) $(TARGETDIR)/cluster.graphml.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/cluster.graphml.gz: $(TARGETDIR)/keys.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	java $(JVM_OPTS) $(JVM_OPTS_HEAVY) $(run-groovy) scripts/BreadthFirstSearch.groovy \
	--input $< \
	--compress --output $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)

.PHONY: cluster-mapping
cluster-mapping: $(TARGETDIR) $(TARGETDIR)/cluster-mapping.csv.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/cluster-mapping.csv.gz: $(TARGETDIR)/cluster.graphml.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	gunzip -c $<\
	| grep "^<node" \
	| sed -n 's:.*<data.*>\(.*\)</data><data.*>\(.*\)</data>.*:\1 \2:p' \
	| gzip \
	> $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)

.PHONY: cluster-sorted-by-name
cluster-sorted-by-name: $(TARGETDIR) $(TARGETDIR)/cluster-sorted-by-name.csv.gz

$(TARGETDIR)/cluster-sorted-by-name.csv.gz: $(TARGETDIR)/cluster-mapping.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	gunzip -c $<\
	| LC_ALL=C sort -k1,1 -S3G --compress-program=gzip\
	| gzip\
	> $@


.PHONY: cluster-sorted-by-value
cluster-sorted-by-value: $(TARGETDIR) $(TARGETDIR)/cluster-sorted-by-value.csv.gz

$(TARGETDIR)/cluster-sorted-by-value.csv.gz: $(TARGETDIR)/cluster-mapping.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	gunzip -c $<\
	| LC_ALL=C sort -n -k2,2 -S3G --compress-program=gzip\
	| gzip\
	> $@


.PHONY: idn-list
idn-list: cluster-idn-list


.PHONY: cluster-idn-list
cluster-idn-list: $(TARGETDIR) $(TARGETDIR)/cluster-idn-list.csv.gz

$(TARGETDIR)/cluster-idn-list.csv.gz: $(TARGETDIR)/cluster-sorted-by-value.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	gunzip -c $< \
	| awk '{ print $$2 " " $$1 }' \
	| java $(run-groovy) scripts/CollectValues.groovy \
	  --input - \
	  --output - \
	| gzip\
	> $@


.PHONY: cluster-size
cluster-size: $(TARGETDIR) $(TARGETDIR)/cluster-size.csv.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/cluster-size.csv.gz: $(TARGETDIR)/cluster-mapping.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	gunzip -c $<\
	| cut -d" " -f2\
	| sort -n -S3G --compress-program=gzip\
	| uniq -c\
	| awk '{print "cluster " $$2 " size " $$1}'\
	| gzip\
	> $@


.PHONY: cluster-size-frequency
cluster-size-frequency: $(TARGETDIR) $(TARGETDIR)/cluster-size-frequency.csv.gz

$(TARGETDIR)/cluster-size-frequency.csv.gz: $(TARGETDIR)/cluster-size.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	gunzip -c $<\
	| cut -d" " -f4\
	| sort -n -S3G --compress-program=gzip\
	| uniq -c\
	| awk '{print  "size " $$2 " freq " $$1}'\
	| gzip\
	> $@


.PHONY: bundle
bundle: $(TARGETDIR) $(TARGETDIR)/bundle.xml.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/bundle.xml.gz: $(TARGETDIR)/excerpts-mapped.mrc.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	java $(JVM_OPTS) $(run-groovy) scripts/WorkflowExcerptsBundle.groovy \
	  --input $< \
	  --algorithm $(RULE) \
	  --morph metamorph/bundle.xml \
	  --xsl xslt/bundle.xsl \
	| java $(run-groovy) scripts/PrettyPrintXml.groovy \
	  --input - \
	  --output - \
	| gzip\
	> $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)


.PHONY: aggregate
aggregate: $(TARGETDIR) $(TARGETDIR)/aggregate.marcxml.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/aggregate.marcxml.gz: $(TARGETDIR)/excerpts-mapped.mrc.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	java $(JVM_OPTS) $(run-groovy) scripts/WorkflowExcerptsAggregate.groovy \
	  --input $< \
	  --limit $(EXPORTLIMIT) \
	  --output - \
	| java $(run-groovy) scripts/PrettyPrintXml.groovy \
	  --input - \
	  --output - \
	| gzip\
	> $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)


.PHONY: excerpts
excerpts: $(TARGETDIR) $(TARGETDIR)/excerpts.mrc.csv.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/excerpts.mrc.csv.gz:
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	find $(DATADIR) -type f \
	| grep "\.gz$$" \
	| xargs gunzip -c \
	| java $(JVM_OPTS) $(run-groovy) scripts/WorkflowExcerpts.groovy \
	 --input - \
	 --id-morph metamorph/gviId.xml \
	 --excerpt-morph metamorph/excerpt.xml \
	 --threads $(THREADS) \
	| LC_ALL=C sort --unique -k1,1 -S3G --compress-program=gzip\
	| gzip\
	> $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)


.PHONY: excerpts-mapped
excerpts-mapped: $(TARGETDIR) $(TARGETDIR)/excerpts-mapped.mrc.csv.gz

.DELETE_ON_ERROR:
$(TARGETDIR)/excerpts-mapped.mrc.csv.gz: $(TARGETDIR)/excerpts.mrc.csv.gz $(TARGETDIR)/cluster-sorted-by-name.csv.gz
	@echo
	@echo
	@echo "[Build of $@]"
	@echo

	java $(JVM_OPTS) $(run-groovy) scripts/WorkflowExcerptsMapping.groovy \
	 --input $< \
	 --map $(word 2,$^) \
	| LC_ALL=C sort --numeric-sort -S3G -k1,1 --compress-program=gzip\
	| gzip\
	> $@

	# Exit 1 when target is empty
	$(call false-if-empty,$@)
