# Makefile

all: run

.PHONY: all

CLEAR_INPUT += ./generated
CLEAR_INPUT += ./obj_dir
CLEAR_INPUT += ./logs
CLEAR_INPUT += ./test_run_dir

TOP_HOME = mycpu_top.sv

clean:
	rm -rf $(CLEAR_INPUT)

define REPLACE_COMMAND
sed -i 's/\bclock\b/aclk/g' $(TOP_HOME)
sed -i 's/\breset\b/aresetn/g' $(TOP_HOME)
endef
# sed -i 's/\bint_0\b/int/g' $(TOP_HOME)
# sed -i 's/\bassign CoreTop_reset = resetn\b/assign CoreTop_reset = ~resetn/g' $(TOP_HOME)

CPY_HOME1 = ./soc-simulator/mycpu/mycpu_top.sv
CPY_HOME2 = /mnt/e/crpboy/file/NSCSCC/cpu-resources/lab/lab/lab11/CPU_CDE_AXI/mycpu_axi_verify/rtl/myCPU/mycpu_top.v

define COPYFILE_COMMAND
cp $(TOP_HOME) $(CPY_HOME1)
cp $(TOP_HOME) $(CPY_HOME2)
endef

SIMULATOR_HOME = ./soc-simulator
# SIMULATOR_HOME = ./simulator/verilator
# SIMULATOR_HOME = /mnt/e/crpboy/file/NSCSCC/soc-simulator

define SOC_SIM_WAVE_COMMAND
cd $(SIMULATOR_HOME) && make wave
endef

define SOC_SIM_ASK_TO_WAVE_COMMAND
@read -p "simulation is over, ok to open vcd file? [y/n] " answer; \
    if [ "$$answer" = "y" ]; then \
				$(SOC_SIM_WAVE_COMMAND); \
    else \
        echo "open cancelled"; \
    fi
endef

define SOC_SIM_COMMAND
cd $(SIMULATOR_HOME) && make clean
cd $(SIMULATOR_HOME) && make
cd $(SIMULATOR_HOME) && make trace
$(SOC_SIM_ASK_TO_WAVE_COMMAND)
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

test:
	clean
	sbt test

run:
	$(REPLACE_COMMAND)
	$(COPYFILE_COMMAND)
	$(SOC_SIM_COMMAND)

wave:
	$(SOC_SIM_WAVE_COMMAND)