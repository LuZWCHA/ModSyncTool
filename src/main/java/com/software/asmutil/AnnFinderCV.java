package com.software.asmutil;


import com.sun.xml.internal.ws.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 陆正威 on 2018/7/13.
 * 速度太慢...
 */
public class AnnFinderCV implements ClassVisitor {

    public List<AnnotationNode> visibleAnnotations;
    public List<AnnotationNode> invisibleAnnotations;


    @Override
    public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {

    }

    @Override
    public void visitSource(String s, String s1) {

    }

    @Override
    public void visitOuterClass(String s, String s1, String s2) {

    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        AnnotationNode an = new AnnotationNode(s);
        if (b) {
            if (visibleAnnotations == null) {
                visibleAnnotations = new ArrayList<AnnotationNode> (1);
            }
            visibleAnnotations.add(an);
        } else {
            if (invisibleAnnotations == null) {
                invisibleAnnotations = new ArrayList<AnnotationNode>(1);
            }
            invisibleAnnotations.add(an);
        }
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {

    }


    @Override
    public void visitInnerClass(String s, String s1, String s2, int i) {

    }

    @Override
    public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
        return null;
    }

    @Override
    public void visitEnd() {

    }
}
