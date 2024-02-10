# Java Method Invocation Printer

## Introduction
This is a simple Java method invocation printer. It is a simple tool that can be used to print the method invocations of a Java class. It is useful for debugging and understanding the flow of a Java program.


## Usage
To use the tool, simply run the following command:
```
java -jar method-invocations-1.0.0-jar-with-dependencies.jar -m <method_name> -t <TEST | APP> -p </path/to/maven/project | pom.xml>
```

## Output
The tool will print the method invocations of the specified method. The output will be in the json format the output will also be saved in a file called `call_hierarchy.json` in the current directory.
```
$ java -jar method-invocations-1.0.0-jar-with-dependencies.jar -m org.jfree.chart.JFreeChart.createBufferedImage -t APP -p /path/to/maven/project

output:
[
    {
        "method": "createBufferedImage(int,int)",
        "sub_hierarchy": [
            {
                "method": "createBufferedImage(int,int,org.jfree.chart.ChartRenderingInfo)",
                "sub_hierarchy": [
                    {
                        "method": "createBufferedImage(int,int,int,org.jfree.chart.ChartRenderingInfo)",
                        "sub_hierarchy": []
                    }
                ]
            }
        ]
    },
    ...
]
```

## NOTE (May change in the future)
- Only work with Java 11 projects.
- Only work with Maven projects.


## Speacial Thanks
- [Spoon](https://github.com/INRIA/spoon)
- [pbadenski - call-hierarchy-printer](https://github.com/pbadenski/call-hierarchy-printer)
