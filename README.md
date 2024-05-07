# Crpboy-MIPS

## 说明

本项目仍在开发中

本项目主要参照的项目：[PUA-MIPS](https://github.com/Clo91eaf/PUA-MIPS) [amandeus mips](https://github.com/amadeus-mips/amadeus-mips/)

## 开发环境

scala version: 2.13.8

chisel version: 6.3.0

## 运行方式

### 清除

使用此命令来清除已经生成的文件，一般不需要使用

```
make clean
```

### soc-simulator中运行

```
sbt run
make
make sim # this will open the .vcd file with gtk wave
```

### vivado testbench中运行

```
sbt run
make submit
```

运行成功后在`./generated`目录下的`mycpu_top.v`就是对应的生成文件

通过修改Makefile中的路径，可以在vivado当中看到提交的mycpu_top.v，运行仿真即可