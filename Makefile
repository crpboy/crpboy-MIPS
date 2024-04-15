# Makefile

default: make

all: clean run test

CLEAR_INPUT += ./generated
CLEAR_INPUT += ./obj_dir
CLEAR_INPUT += ./logs
CLEAR_INPUT += ./test_run_dir

clean:
	rm -rf $(CLEAR_INPUT)

run:
	sbt run

test:
	sbt test


# [[discard]]

# NAME = VCrpboyMips

# ifeq ($(VERILATOR_ROOT),)
# VERILATOR = verilator
# VERILATOR_COVERAGE = verilator_coverage
# else
# export VERILATOR_ROOT
# VERILATOR = $(VERILATOR_ROOT)/bin/verilator
# VERILATOR_COVERAGE = $(VERILATOR_ROOT)/bin/verilator_coverage
# endif

# VERILATOR_FLAGS += -cc --exe
# VERILATOR_FLAGS += -x-assign fast
# VERILATOR_FLAGS += --trace
# VERILATOR_FLAGS += --assert
# VERILATOR_FLAGS += --coverage

# VERILATOR_INPUT += -f input.vc
# VERILATOR_INPUT += ./generated/CrpboyMips.v
# VERILATOR_INPUT += sim_main.cpp

# sim_with_cpp:
# 	@echo
# 	@echo "-- VERILATE ----------------"
# 	$(VERILATOR) $(VERILATOR_FLAGS) $(VERILATOR_INPUT)

# 	@echo
# 	@echo "-- BUILD -------------------"
# 	$(MAKE) -j -C obj_dir -f ../Makefile_obj

# 	@echo
# 	@echo "-- RUN ---------------------"
# 	@rm -rf logs
# 	@mkdir -p logs
# 	obj_dir/${NAME} +trace

# 	@echo
# 	@echo "-- COVERAGE ----------------"
# 	@rm -rf logs/annotated
# 	$(VERILATOR_COVERAGE) --annotate logs/annotated logs/coverage.dat

# 	@echo
# 	@echo "-- SIMULATION --------------------"
# 	gtkwave ./logs/vlt_dump.vcd
