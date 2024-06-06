# Makefile

all: trace

.PHONY: all

CLEAR_INPUT += ./generated
CLEAR_INPUT += ./obj_dir
CLEAR_INPUT += ./logs
CLEAR_INPUT += ./test_run_dir

TOP_HOME = mycpu_top.sv

clean:
	rm -rf $(CLEAR_INPUT)

define REPLACE_COMMAND
@sed -i 's/\bclock\b/aclk/g' $(TOP_HOME)
@sed -i 's/\breset\b/aresetn/g' $(TOP_HOME)
@sed -i '/xilinx_single_port_ram_read_first.sv/d' $(TOP_HOME)
@echo "replace done."
endef
# sed -i 's/\bint_0\b/int/g' $(TOP_HOME)
# sed -i 's/\bassign CoreTop_reset = resetn\b/assign CoreTop_reset = ~resetn/g' $(TOP_HOME)

SIMULATOR_HOME = ./simulator/work
CPY_HOME1 = ./simulator/resource/mycpu/mycpu_top.sv
CPY_HOME2 = /mnt/e/crpboy/file/NSCSCC/cpu-resources/lab/lab/lab11/CPU_CDE_AXI/mycpu_axi_verify/rtl/myCPU/mycpu_top.v

define COPYFILE_COMMAND
@cp $(TOP_HOME) $(CPY_HOME1)
@cp $(TOP_HOME) $(CPY_HOME2)
@echo "copy done."
endef

define SOC_SIM_WAVE_COMMAND
@echo "opening wave file..."
@cd $(SIMULATOR_HOME) && make wave
endef

define GENERATE_COMMAND
$(REPLACE_COMMAND)
$(COPYFILE_COMMAND)
@cd $(SIMULATOR_HOME) && make clean
@cd $(SIMULATOR_HOME) && make
endef

replace:
	$(REPLACE_COMMAND)

copyfile:
	$(COPYFILE_COMMAND)

vivado:
	rm -rf $(CLEAR_INPUT)
	sbt run
	$(REPLACE_COMMAND)
	$(COPYFILE_COMMAND)

submit:
	$(REPLACE_COMMAND)
	$(COPYFILE_COMMAND)
	@echo "submit successfully!"

gene:
	$(GENERATE_COMMAND)

trace:
	$(GENERATE_COMMAND)
	@cd $(SIMULATOR_HOME) && make trace

perf:
	$(GENERATE_COMMAND)
	@cd $(SIMULATOR_HOME) && make perf

diff:
	$(GENERATE_COMMAND)
	@cd $(SIMULATOR_HOME) && make perfdiff

wave:
	@$(SOC_SIM_WAVE_COMMAND)

count:
	@echo "count the lines"
	@find ./src -name "*.scala" | xargs wc -l