# Chemdah

<div align="center">

![Logo](https://github.com/shuiqing2046/Chemdah/assets/28628358/4bae5ec8-a24a-4a9b-bdaf-3aaaaa2a9918)

![Paper](https://cdn.jsdelivr.net/gh/intergrav/devins-badges/assets/compact/supported/paper_vector.svg)
![Purpur](https://cdn.jsdelivr.net/gh/intergrav/devins-badges/assets/compact/supported/purpur_vector.svg)

[![Chemdah Release](https://github.com/shuiqing2046/Chemdah/actions/workflows/build.yml/badge.svg)](https://github.com/shuiqing2046/Chemdah/actions/workflows/release.yml)
  <p align="center">
    <strong style="color:#999999;">Attempt to actively maintain this resource.</strong>
    <br />
    <br />
    <a href="https://github.com/shuiqing2046/Chemdah/actions"><strong><span style="color:#009900;">Download Github Actions</span></strong></a>
    <br />
  </p>

</div>

<details>
<summary>Description</summary>

---

Chemdah 似乎已被放弃并不再提供访问,因而我尝试维护后续版本的简单支持性更新.

Chemdah seems to have been abandoned and no longer provides access, so I am attempting to maintain a simple support update for subsequent versions.

---

Chemdah 是免费的，但我们不提供 jar 文件，你可以通过以下步骤自行构建插件。

**Windows 平台**
```shell
gradlew.bat clean build
```

**macOS/Linux 平台**
```shell
./gradlew clean build
```

# Chemdah 对比 其他插件
> 数据来源: BetonQuest

| Feature | Chemdah                    | BetonQuest | Quests | BeautyQuests | QuestCreator | MangoQuest |
| --- |----------------------------| --- | --- | --- | --- | --- |
| 免费使用 | ✅ (¥198)                   | ✅ | ✅ | ✅ | ❌ (¥131.25) | ✅
| 开源 | ✅                          | ✅ | ✅ | ✅ | ❌ | ✅
| API | ✅                          | ✅ | ✅ | ✅ | ❌ (闭源) | ✅
| 版本支持 | 1.9-1.20.1                 | 1.13.2-1.16.5 | 1.7-1.16.5 | 1.11-1.16.5 | 1.7-1.16.5 | 1.13-1.16.5
| 数据库支持 | SQLite & MySQL & MongoDB   | SQLite & MySQL | ❌ | MySQL | MySQL | MySQL & MongoDB
| 多人任务 | ✅                          | ❌ | ❌ | ❌ | ❓ | ❌
| 多分支对话 | ✅                          | ✅ | ❓ | ❓| ✅ | ❓
| 客户端 NPC | ✅ (Adyeshach)              | ✅ | ❓ | ❓| ✅ | ❓
| 有组织的文件结构 | ✅                          | ✅ | ❌ | ❓ | ✅ | ✅
| 基于可编程的脚本 | ✅                          | ❌ | ❌ | ❌ | ❌ | ❌
| 自动化任务分发 | ✅ 自动化组件 (Automation Addon) | ❌ 全局事件 (Static Events) | ✅ 计划任务 (Planner) | ❌ | ✅ 任务激活器 (Activators) | ❌
| 沉浸式任务追踪 | ✅ 追踪组件 (Track Addon)       | ❌ 任务手册 (Journal) | ❌ | ❌ | ❌ | ❌
| 自定义通知系统 | ✅ 脚本代理 (Kether)            | ✅ Notify IO | ❌ | ❌ | ❌ | ❌

**JavaDoc**：https://jd.ptms.ink/chemdah

**[Blockdb [Download]](http://ptms.ink:8081/repository/maven-releases/ink/ptms/Blockdb/1.1.0/Blockdb-1.1.0.jar)**
</details>