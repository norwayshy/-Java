import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

// 哈夫曼节点类
class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left, right;

    HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return Integer.compare(this.frequency, o.frequency);
    }

    // 打印哈夫曼树
    public void printTree(String indent, JTextArea logArea) {
        if (this != null) {
            if (this.character != '\0') {
                logArea.append(indent + "叶节点: " + character + " (频率: " + frequency + ")\n");
            } else {
                logArea.append(indent + "内部节点 (频率: " + frequency + ")\n");
            }

            if (this.left != null) {
                this.left.printTree(indent + "  ", logArea);
            }

            if (this.right != null) {
                this.right.printTree(indent + "  ", logArea);
            }
        }
    }
}

// 主程序类
public class HuffmanXORApp extends JFrame {
    private JTextArea logArea;
    private JButton encodeButton, decodeButton, selectFileButton, compressButton, decompressButton;
    private File selectedFile;
    private JButton createFileButton;

    public HuffmanXORApp() {
        setTitle("哈夫曼编码与加密工具");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建组件
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        encodeButton = new JButton("编码");
        decodeButton = new JButton("解码");
        selectFileButton = new JButton("选择文件");
        compressButton = new JButton("压缩");
        decompressButton = new JButton("解压");
        createFileButton = new JButton("新建文件");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(selectFileButton);
        buttonPanel.add(encodeButton);
        buttonPanel.add(decodeButton);
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);
        buttonPanel.add(createFileButton); // 添加新按钮

        // 添加组件
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 初始化按钮状态
        encodeButton.setEnabled(false);
        decodeButton.setEnabled(false);
        compressButton.setEnabled(false);
        decompressButton.setEnabled(false);

        // 添加按钮事件
        selectFileButton.addActionListener(e -> selectFile());
        encodeButton.addActionListener(e -> encodeFile());
        decodeButton.addActionListener(e -> decodeFile());
        compressButton.addActionListener(e -> compressFile());
        decompressButton.addActionListener(e -> decompressFile());
        createFileButton.addActionListener(e -> createNewFile());
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));  // 设置当前目录为当前工作目录

        // 设置文件过滤器，只显示 .source 和 .code 文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Zip, Souce and Code files", "souce", "code","zip");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();

            if (fileName.endsWith(".souce")) {
                log("已选择源文件：" + fileName);
                encodeButton.setEnabled(true);
                decodeButton.setEnabled(false);
                compressButton.setEnabled(true);
                decompressButton.setEnabled(false);
            } else if (fileName.endsWith(".code")) {
                log("已选择编码文件：" + fileName);
                encodeButton.setEnabled(false);
                decodeButton.setEnabled(true);
                compressButton.setEnabled(true);
                decompressButton.setEnabled(false);
            } else if (fileName.endsWith(".zip")) {
                log("已选择压缩文件：" + fileName);
                encodeButton.setEnabled(false);
                decodeButton.setEnabled(false);
                compressButton.setEnabled(false);
                decompressButton.setEnabled(true);
            } else {
                log("无效文件类型！");
                encodeButton.setEnabled(false);
                decodeButton.setEnabled(false);
                compressButton.setEnabled(false);
                decompressButton.setEnabled(false);
            }
        }
    }
    private void createNewFile() {
        // 提示用户选择文件类型（.souce 或 .code）
        String[] options = {".souce", ".code"};
        String fileExtension = (String) JOptionPane.showInputDialog(this, "选择文件类型", "新建文件", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (fileExtension == null) {
            return; // 用户取消选择
        }

        // 提示用户输入文件名
        String fileName = JOptionPane.showInputDialog(this, "请输入文件名:");
        if (fileName == null || fileName.isEmpty()) {
            showError("文件名不能为空！");
            return;
        }

        // 设置文件名和扩展名
        File newFile = new File(fileName + fileExtension);

        // 打开一个新的窗口供用户输入文本
        JTextArea textArea = new JTextArea(20, 40); // 文本输入区域
        JScrollPane scrollPane = new JScrollPane(textArea);
        int option = JOptionPane.showConfirmDialog(this, scrollPane, "请输入文本", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            try {
                // 保存文本到文件
                String text = textArea.getText();
                saveFile(newFile, text);
                log("文件已保存：" + newFile.getAbsolutePath());
            } catch (IOException e) {
                showError("保存文件失败：" + e.getMessage());
            }
        }
    }
    private void saveFile(File file, String content) throws IOException {
        String fileName = file.getName();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (fileName.endsWith(".code")) {
                content = "PLAIN\n" + content;
            }
            writer.write(content);
        }
    }


    private void encodeFile() {
        if (selectedFile == null) {
            showError("未选择文件！");
            return;
        }
        try {
            // 读取文件内容，包括空格和换行符
            String content = readFile(selectedFile);

            if (content.isEmpty()) {
                showError("文件内容为空，无法编码！");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean encrypt = JOptionPane.showConfirmDialog(this, "是否加密文件？", "加密选项", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        String key = null;

        if (encrypt) {
            key = JOptionPane.showInputDialog(this, "请输入加密密钥：");
            if (key == null || key.isEmpty()) {
                showError("密钥不能为空！");
                return;
            }
        }

        try {
            // 读取文件内容，包括空格和换行符
            String content = readFile(selectedFile);

            // 计算频率
            Map<Character, Integer> frequencies = calculateFrequencies(content);

            // 构建哈夫曼树
            HuffmanNode root = buildHuffmanTree(frequencies);

            // 生成哈夫曼编码
            Map<Character, String> huffmanCodes = generateCodes(root);

            // 打印哈夫曼树
            logArea.setText(""); // 清空日志区域
            log("哈夫曼树结构：");
            root.printTree("", logArea);

            // 输出每个字符的哈夫曼编码
            log("每个字符的哈夫曼编码：");
            for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                log(entry.getKey() + ": " + entry.getValue());
            }

            // 编码内容
            String encodedContent = encodeContent(content, huffmanCodes);
            //输出编码结果
            log("编码结果：\n" + encodedContent);

            // 如果加密，则进行加密
            if (encrypt) {
                encodedContent = xorEncryptDecrypt(encodedContent, key);
            }

            // **保存频率表到 .tree 文件**【修改】
            String treeFileName = replaceFileExtension(selectedFile, ".tree");
            saveFrequencyTable(treeFileName, frequencies);

            // 保存编码文件，只保存编码内容，不保存频率表
            String outputFileName = replaceFileExtension(selectedFile, ".code");
            saveEncodedFile(outputFileName, encodedContent, encrypt);

            log("编码完成！保存到：" + outputFileName);
        } catch (IOException e) {
            showError("编码失败：" + e.getMessage());
        }
    }
    private void saveFrequencyTable(String fileName, Map<Character, Integer> frequencies) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
                char character = entry.getKey();
                int frequency = entry.getValue();

                // 将换行符替换为 "NL" 标记
                if (character == '\n') {
                    writer.write("NL" + ":" + frequency + "\n");
                } else {
                    writer.write(character + ":" + frequency + "\n");
                }
            }
        }
    }





    private void decodeFile() {
        if (selectedFile == null) {
            showError("未选择文件！");
            return;
        }

        // **手动选择 .tree 文件**【修改】
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));  // 设置当前目录为当前工作目录
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Tree files", "tree");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File treeFile = fileChooser.getSelectedFile();
            Map<Character, Integer> frequencies = readFrequencyTable(treeFile);

            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                boolean encrypted = reader.readLine().equals("ENCRYPTED");
                StringBuilder encodedContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    encodedContent.append(line);  // 读取编码内容
                }
                reader.close();

                // 如果文件是加密的，先解密
                if (encrypted) {
                    String key = JOptionPane.showInputDialog(this, "请输入解密密钥：");
                    if (key == null || key.isEmpty()) {
                        showError("密钥不能为空！");
                        return;
                    }
                    encodedContent = new StringBuilder(xorEncryptDecrypt(encodedContent.toString(), key));
                }

                // 使用频率表构建哈夫曼树
                HuffmanNode root = buildHuffmanTree(frequencies);
                String decodedContent = decodeContent(encodedContent.toString(), root);

                // 保存解码后的内容
                String outputFileName = replaceFileExtension(selectedFile, ".decode");
                writeFile(outputFileName, decodedContent);

                log("解码结果：\n" + decodedContent);
                log("解码完成！保存到：" + outputFileName);
            } catch (IOException e) {
                showError("解码失败：" + e.getMessage());
            }
        }
    }

    private void compressFile() {
        if (selectedFile == null) {
            showError("未选择文件！");
            return;
        }

        try {
            //String outputFileName = replaceFileExtension(selectedFile, ".zip");
            String outputFileName = selectedFile + ".zip";
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 FileOutputStream fos = new FileOutputStream(outputFileName);
                 GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    gzipOS.write(buffer, 0, length);
                }
            }
            log("文件压缩完成！保存到：" + outputFileName);
        } catch (IOException e) {
            showError("压缩失败：" + e.getMessage());
        }
    }

    private void decompressFile() {
        if (selectedFile == null) {
            showError("未选择文件！");
            return;
        }

        try {
            String outputFileName = replaceFileExtension(selectedFile, "");
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 GZIPInputStream gis = new GZIPInputStream(fis);
                 FileOutputStream fos = new FileOutputStream(outputFileName)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = gis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
            log("文件解压完成！保存到：" + outputFileName);
        } catch (IOException e) {
            showError("解压失败：" + e.getMessage());
        }
    }

    private String xorEncryptDecrypt(String content, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            result.append((char) (content.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }

    private Map<Character, Integer> calculateFrequencies(String content) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : content.toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }
        return frequencies;
    }


    private HuffmanNode buildHuffmanTree(Map<Character, Integer> frequencies) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
            queue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();
            HuffmanNode parent = new HuffmanNode('\0', left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            queue.add(parent);
        }
        return queue.poll();
    }

    private Map<Character, String> generateCodes(HuffmanNode root) {
        Map<Character, String> codes = new HashMap<>();
        generateCodesRecursive(root, "", codes);
        return codes;
    }

    private void generateCodesRecursive(HuffmanNode node, String code, Map<Character, String> codes) {
        if (node == null) return;
        if (node.character != '\0') codes.put(node.character, code);

        generateCodesRecursive(node.left, code + "0", codes);
        generateCodesRecursive(node.right, code + "1", codes);
    }
    private String encodeContent(String content, Map<Character, String> codes) {
        StringBuilder encoded = new StringBuilder();
        for (char c : content.toCharArray()) {
            if (codes.containsKey(c)) {
                encoded.append(codes.get(c)); // 使用哈夫曼编码
            } else {
                throw new IllegalArgumentException("编码中缺少字符: " + c);
            }
        }
        return encoded.toString();
    }


    private String decodeContent(String encodedContent, HuffmanNode root) {
        StringBuilder decoded = new StringBuilder();
        HuffmanNode current = root;

        // 逐位解码
        for (char c : encodedContent.toCharArray()) {
            current = (c == '0') ? current.left : current.right;
            if (current.left == null && current.right == null) { // 到达叶子节点
                decoded.append(current.character); // 解码出的字符
                current = root; // 回到根节点
            }
        }

        return decoded.toString();
    }



    private void saveEncodedFile(String fileName, String encodedContent, boolean encrypted) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(encrypted ? "ENCRYPTED\n" : "PLAIN\n");

        writer.write(encodedContent);  // 保存编码内容

        writer.close();
    }


    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n"); // 保留换行符
            }
        }
        // 删除最后一个多余的换行符（可选）
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
        }
        return content.toString();
    }

    // **读取频率表的方法**
    private Map<Character, Integer> readFrequencyTable(File file) {
        Map<Character, Integer> frequencies = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        // 如果是 "NL"，则恢复为换行符
                        char character = parts[0].equals("NL") ? '\n' : parts[0].charAt(0);
                        int frequency = Integer.parseInt(parts[1].trim());
                        frequencies.put(character, frequency);
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的行
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("读取频率表文件时出错：" + e.getMessage());
        }
        return frequencies;
    }





    private void writeFile(String fileName, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(content);
        writer.close();
    }

    private String replaceFileExtension(File file, String newExtension) {
        String fileName = file.getName();
        return file.getParent() + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + newExtension;
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HuffmanXORApp app = new HuffmanXORApp();
            app.setVisible(true);
        });
    }
}
