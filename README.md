# Crpboy-MIPS

## 说明

本项目仍在开发中

本项目主要参照的项目：[PUA-MIPS](https://github.com/Clo91eaf/PUA-MIPS) [amandeus mips](https://github.com/amadeus-mips/amadeus-mips/)

## 项目文件结构
```
src
├── main
│   └── scala
│       ├── Elaborate.scala               生成.v文件的main
│       └── cpu                           cpu代码
│           ├── common                    保存了一些常量和Bundle的定义
│           ├── core                      核心
│           │   ├── cache                 缓存
│           │   └── pipeline              流水线
│           │       └── components        流水线组件
│           │           ├── decode        译码阶段
│           │           ├── execute       执行阶段
│           │           ├── fetch         取指阶段
│           │           ├── memory        访存阶段
│           │           └── writeback     写回阶段
│           ├── mycpu_top.scala           顶层IO接口
│           └── utils                     实用函数
└── test                                  测试用
    └── scala
        └── test.scala
```