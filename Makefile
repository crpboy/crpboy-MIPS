# Makefile

all: run

.PHONY: all

CLEAR_INPUT += ./generated
CLEAR_INPUT += ./obj_dir
CLEAR_INPUT += ./logs
CLEAR_INPUT += ./test_run_dir

clean:
	rm -rf $(CLEAR_INPUT)

CPY_HOME1 = /mnt/e/crpboy/file/NSCSCC/cpu-resources/lab/lab/lab3/CPU_CDE/mycpu_verify/rtl/myCPU/mycpu_top.v

define REPLACE_COMMAND
sed -i 's/\bclock\b/clk/g' ./generated/mycpu_top.v
sed -i 's/\breset\b/resetn/g' ./generated/mycpu_top.v
sed -i 's/\bassign CoreTop_reset = resetn\b/assign CoreTop_reset = ~resetn/g' ./generated/mycpu_top.v
endef

replace:
	$(REPLACE_COMMAND)

define COPYFILE_COMMAND
cp ./generated/mycpu_top.v $(CPY_HOME1)
endef

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

define SOC_SIM_WAVE_COMMAND
cd /mnt/e/crpboy/file/NSCSCC/soc-simulator && gtkwave trace.vcd config.gtkw
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
cd /mnt/e/crpboy/file/NSCSCC/soc-simulator && make clean
cd /mnt/e/crpboy/file/NSCSCC/soc-simulator && make
cd /mnt/e/crpboy/file/NSCSCC/soc-simulator && make run
$(SOC_SIM_ASK_TO_WAVE_COMMAND)
endef

run:
	$(REPLACE_COMMAND)
	$(COPYFILE_COMMAND)
	$(SOC_SIM_COMMAND)

wave:
	$(SOC_SIM_WAVE_COMMAND)