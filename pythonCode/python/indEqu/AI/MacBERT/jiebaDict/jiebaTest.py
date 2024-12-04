import jieba
import os

# 自定义词典文件路径
dict_path = 'D:/jena_project1/ontology-construction/pythonCode/python/indEqu/AI/MacBERT/jiebaDict/adjust_dict.txt'

# 检查文件是否存在
if not os.path.exists(dict_path):
    print(f"文件不存在: {dict_path}")
else:
    print(f"文件存在: {dict_path}")

# 手动读取词典文件内容
with open(dict_path, 'r', encoding='utf-8') as f:
    content = f.readlines()
    print(f"词典内容: {content}")

    # 验证词典文件格式
    for line in content:
        parts = line.strip().split(' ')
        if len(parts) != 2:
            print(f"无效的词典条目: {line.strip()}")
            raise ValueError(f"无效的词典条目: {line.strip()}")

# 删除缓存文件
cache_file = os.path.expanduser('~/.jieba.cache')
if os.path.exists(cache_file):
    os.remove(cache_file)

# 设置默认词典
jieba.set_dictionary(dict_path)

# 需要分词的文本
text = "中央处理器CPU"

# 进行分词
words = jieba.lcut(text)

# 输出分词结果
print(words)  # 输出: ['中央', '处理器'