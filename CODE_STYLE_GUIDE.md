# Code Style and Formatting Guide

This project uses automated code style and formatting tools to maintain consistent code quality.

## Tools

### 1. Spotless (Code Formatter)
Spotless automatically formats your code according to Google Java Style Guide.

**Supported file types:**
- Java files (Google Java Format)
- XML/POM files
- YAML files
- Markdown files

**Commands:**
```bash
# Check if code is formatted correctly
mvn spotless:check

# Automatically format all code
mvn spotless:apply
```

**Note:** Spotless check runs automatically during the `compile` phase.

### 2. Checkstyle (Code Quality)
Checkstyle validates code against style rules and best practices.

**Commands:**
```bash
# Run Checkstyle validation
mvn checkstyle:check

# Generate Checkstyle report
mvn checkstyle:checkstyle
```

**Note:** Checkstyle runs automatically during the `validate` phase.

### 3. EditorConfig
The `.editorconfig` file ensures consistent coding styles across different editors and IDEs.

**Supported IDEs:**
- IntelliJ IDEA (built-in support)
- VS Code (install EditorConfig extension)
- Eclipse (install EditorConfig plugin)

## Workflow

### Before Committing
Always format your code before committing:
```bash
mvn spotless:apply
mvn clean verify
```

### During Build
The following checks run automatically:
1. **Validate phase**: Checkstyle validates code quality
2. **Compile phase**: Spotless checks code formatting

### Fix Formatting Issues
If you get Spotless errors during build:
```bash
mvn spotless:apply
```

If you get Checkstyle warnings, review them and fix manually or adjust the `checkstyle.xml` configuration.

## IDE Setup

### IntelliJ IDEA
1. Install the "google-java-format" plugin
2. Enable it in Settings → google-java-format Settings
3. EditorConfig support is built-in

### VS Code
1. Install "Language Support for Java"
2. Install "EditorConfig for VS Code" extension
3. Configure formatter in settings.json

### Eclipse
1. Install "google-java-format" plugin
2. Install "EditorConfig Eclipse" plugin

## Style Guidelines

- **Line Length**: Maximum 120 characters
- **Indentation**: 2 spaces for Java
- **Import Order**: Automatic via Spotless
- **Braces**: Required for all control structures
- **Naming Conventions**: Follow Java standard (camelCase, PascalCase, etc.)

## Configuration Files

- `checkstyle.xml` - Checkstyle rules configuration
- `.editorconfig` - IDE coding style configuration
- `pom.xml` - Maven plugin configurations

## Disabling Checks (Use Sparingly)

### Skip Spotless temporarily:
```bash
mvn clean install -Dspotless.check.skip=true
```

### Skip Checkstyle temporarily:
```bash
mvn clean install -Dcheckstyle.skip=true
```

**Note:** These should only be used for debugging. Always fix formatting issues before committing.
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
        "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
        "https://checkstyle.org/dtds/configuration_1_3.dtd">

<!--
    Checkstyle configuration for Google Java Style Guide
    Based on: https://google.github.io/styleguide/javaguide.html
-->
<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="warning"/>
    <property name="fileExtensions" value="java, properties, xml"/>

    <!-- Excludes all 'module-info.java' files -->
    <module name="BeforeExecutionExclusionFileFilter">
        <property name="fileNamePattern" value="module\-info\.java$"/>
    </module>

    <!-- Checks for whitespace -->
    <module name="FileTabCharacter">
        <property name="eachLine" value="true"/>
    </module>

    <module name="TreeWalker">
        <!-- Naming Conventions -->
        <module name="PackageName">
            <property name="format" value="^[a-z]+(\.[a-z][a-z0-9_]*)*$"/>
        </module>
        <module name="TypeName"/>
        <module name="MemberName"/>
        <module name="MethodName"/>
        <module name="ParameterName"/>
        <module name="LocalVariableName"/>
        <module name="ConstantName"/>

        <!-- Imports -->
        <module name="AvoidStarImport"/>
        <module name="RedundantImport"/>
        <module name="UnusedImports"/>
        <module name="IllegalImport"/>

        <!-- Size Violations -->
        <module name="LineLength">
            <property name="max" value="120"/>
            <property name="ignorePattern" value="^package.*|^import.*|a href|href|http://|https://|ftp://"/>
        </module>
        <module name="MethodLength">
            <property name="max" value="150"/>
        </module>
        <module name="ParameterNumber">
            <property name="max" value="7"/>
            <property name="tokens" value="METHOD_DEF"/>
        </module>

        <!-- Whitespace -->
        <module name="EmptyForIteratorPad"/>
        <module name="GenericWhitespace"/>
        <module name="MethodParamPad"/>
        <module name="NoWhitespaceAfter"/>
        <module name="NoWhitespaceBefore"/>
        <module name="OperatorWrap"/>
        <module name="ParenPad"/>
        <module name="TypecastParenPad"/>
        <module name="WhitespaceAfter"/>
        <module name="WhitespaceAround"/>

        <!-- Modifier Checks -->
        <module name="ModifierOrder"/>
        <module name="RedundantModifier"/>

        <!-- Blocks -->
        <module name="AvoidNestedBlocks"/>
        <module name="EmptyBlock"/>
        <module name="LeftCurly"/>
        <module name="NeedBraces"/>
        <module name="RightCurly"/>

        <!-- Coding -->
        <module name="EmptyStatement"/>
        <module name="EqualsHashCode"/>
        <module name="IllegalInstantiation"/>
        <module name="InnerAssignment"/>
        <module name="MissingSwitchDefault"/>
        <module name="SimplifyBooleanExpression"/>
        <module name="SimplifyBooleanReturn"/>

        <!-- Class Design -->
        <module name="FinalClass"/>
        <module name="HideUtilityClassConstructor"/>
        <module name="InterfaceIsType"/>
        <module name="VisibilityModifier">
            <property name="protectedAllowed" value="true"/>
            <property name="packageAllowed" value="true"/>
        </module>

        <!-- Miscellaneous -->
        <module name="ArrayTypeStyle"/>
        <module name="UpperEll"/>

        <!-- Annotations -->
        <module name="AnnotationLocation">
            <property name="allowSamelineSingleParameterlessAnnotation" value="false"/>
        </module>
    </module>
</module>

