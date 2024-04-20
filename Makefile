# Makefile

all: run

CLEAR_INPUT += ./generated
CLEAR_INPUT += ./obj_dir
CLEAR_INPUT += ./logs
CLEAR_INPUT += ./test_run_dir

clean:
	rm -rf $(CLEAR_INPUT)

CPY_HOME1 = /mnt/e/crpboy/file/NSCSCC/CO-lab-material-CQU-2022/mycpu/mycpu_top.v
CPY_HOME2 = /mnt/e/crpboy/file/NSCSCC/cpu-resources/lab/lab/lab3/CPU_CDE/mycpu_verify/rtl/myCPU/mycpu_top.v

define REPLACE_COMMAND
sed -i 's/\bclock\b/clk/g' ./generated/mycpu_top.v
sed -i 's/\breset\b/resetn/g' ./generated/mycpu_top.v
# sed -i 's/\bassign CoreTop_reset = resetn\b/assign CoreTop_reset = ~resetn/g' ./generated/mycpu_top.v
sed -i 's/\bassign CoreTop_reset = resetn\b/assign CoreTop_reset = ~resetn/g' ./generated/mycpu_top.v
endef

replace:
	$(REPLACE_COMMAND)

define COPYFILE_COMMAND
cp ./generated/mycpu_top.v $(CPY_HOME1)
cp ./generated/mycpu_top.v $(CPY_HOME2)
endef

copyfile:
	$(COPYFILE_COMMAND)

run:
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
