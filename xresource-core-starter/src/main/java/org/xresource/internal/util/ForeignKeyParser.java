package org.xresource.internal.util;

import java.util.*;

import org.xresource.internal.models.ForeignKeyTree;

public class ForeignKeyParser {

    public static ForeignKeyTree parse(String input) {
        ForeignKeyTree root = new ForeignKeyTree();
        if (input == null || input.isEmpty())
            return root;

        int index = 0;
        while (index < input.length()) {
            int start = index;
            while (index < input.length() && input.charAt(index) != ',' && input.charAt(index) != '{') {
                index++;
            }

            String field = input.substring(start, index).trim();
            ForeignKeyTree child = root.children.computeIfAbsent(field, k -> new ForeignKeyTree());

            if (index < input.length() && input.charAt(index) == '{') {
                int braceCount = 1;
                int end = ++index;
                while (end < input.length() && braceCount > 0) {
                    if (input.charAt(end) == '{')
                        braceCount++;
                    else if (input.charAt(end) == '}')
                        braceCount--;
                    end++;
                }

                String nested = input.substring(index, end - 1).trim();
                ForeignKeyTree nestedTree = parse(nested);
                child.children.putAll(nestedTree.children);
                index = end;
            }

            if (index < input.length() && input.charAt(index) == ',')
                index++;
        }

        return root;
    }

    // For testing
    public static void main(String[] args) {
        String test = "owner{team{users}},patchProcessTemplate{owner},team";
        ForeignKeyTree tree = ForeignKeyParser.parse(test);
        printTree(tree, "");
    }

    private static void printTree(ForeignKeyTree tree, String indent) {
        for (Map.Entry<String, ForeignKeyTree> entry : tree.children.entrySet()) {
            System.out.println(indent + "- " + entry.getKey());
            printTree(entry.getValue(), indent + "  ");
        }
    }
}
