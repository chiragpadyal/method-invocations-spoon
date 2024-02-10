package com.autoTestGen;

import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.io.PrintStream;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MethodCallHierarchyBuilder {
    private final CtExecutableReference<?> executableReference;
    private final Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> callList;
    private final Map<CtTypeReference<?>, Set<CtTypeReference<?>>> classHierarchy;

    private MethodCallHierarchyBuilder(CtExecutableReference<?> executableReference,
                                       Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> callList,
                                       Map<CtTypeReference<?>, Set<CtTypeReference<?>>> classHierarchy) {
        this.executableReference = executableReference;
        this.callList = callList;
        this.classHierarchy = classHierarchy;
    }

    public static List<MethodCallHierarchyBuilder> forMethodName(String methodName,
                                                           Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> callList,
                                                           Map<CtTypeReference<?>, Set<CtTypeReference<?>>> classHierarchy) {
        ArrayList<MethodCallHierarchyBuilder> result = new ArrayList<>();
        for (CtExecutableReference<?> executableReference : findExecutablesForMethodName(methodName, callList)) {
            result.add(new MethodCallHierarchyBuilder(executableReference, callList, classHierarchy));
        }
        return result;
    }

    static List<CtExecutableReference<?>> findExecutablesForMethodName(String methodName, Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> callList) {
        ArrayList<CtExecutableReference<?>> result = new ArrayList<>();
        for (CtExecutableReference<?> executableReference : callList.keySet()) {
            String executableReferenceMethodName = executableReference.getDeclaringType().getQualifiedName() + "." + executableReference.getSimpleName();
            // System.out.println("executableReferenceMethodName: " + executableReferenceMethodName);
            if (executableReferenceMethodName.equals(methodName)
                    || executableReference.toString().contains(methodName)
                    || executableReference.toString().matches(methodName)) {
                result.add(executableReference);
            }
        }
        return result;
    }

    public ArrayNode printCallHierarchy(PrintStream printStream) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode hierarchyArray = mapper.createArrayNode();
        printCallHierarchy(hierarchyArray, executableReference, new HashSet<>(), mapper, 3);
        return hierarchyArray;
    }
    
    private void printCallHierarchy(ArrayNode hierarchyArray, CtExecutableReference<?> method, Set<CtExecutableReference<?>> alreadyVisited, ObjectMapper mapper, int depth) {
        if (depth == 0 || alreadyVisited.contains(method)) {
            return;
        }
        alreadyVisited.add(method);
        
        ObjectNode methodNode = mapper.createObjectNode();
        methodNode.put("method", method.toString());
        ArrayNode subHierarchyArray = mapper.createArrayNode();
        methodNode.set("sub_hierarchy", subHierarchyArray);
        hierarchyArray.add(methodNode);
        
        List<CtExecutableReference<?>> callListForMethod = callList.get(method);
        if (callListForMethod != null) {
            for (CtExecutableReference<?> eachReference : callListForMethod) {
                printCallHierarchy(subHierarchyArray, eachReference, alreadyVisited, mapper, depth - 1);
            }
        }
        
        Set<CtTypeReference<?>> subclasses = classHierarchy.get(method.getDeclaringType());
        if (subclasses != null) {
            for (CtTypeReference<?> subclass : subclasses) {
                CtExecutableReference<?> reference = method.getOverridingExecutable(subclass);
                if (reference != null && isSameModule(method, reference)) {
                    printCallHierarchy(subHierarchyArray, reference, alreadyVisited, mapper, depth - 1);
                }
            }
        }
    }
    
    private String getBasePackage(String fullPackageName) {
        String[] packageParts = fullPackageName.split("\\.");
        return packageParts[0] + "." + packageParts[1];
    }
    
    private boolean isSameModule(CtExecutableReference<?> method1, CtExecutableReference<?> method2) {

        String basePackage1 = getBasePackage(method1.getDeclaringType().getPackage().getQualifiedName());
        String basePackage2 = getBasePackage(method2.getDeclaringType().getPackage().getQualifiedName());
        
        // Check if base package names are equal
        return basePackage1.equals(basePackage2);
    }
    
}
