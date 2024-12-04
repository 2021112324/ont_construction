# -*- coding: utf-8 -*-
from transformers import BertTokenizer, BertModel
from sklearn.metrics.pairwise import cosine_similarity
import torch
import sys
import re
import jieba
import math
import os

REPETITIONCOEFFICIENT = 0.1

# 加载MacBERT模型和tokenizer
model_name = "hfl/chinese-macbert-base"  # 可以根据需要选择MacBERT的不同版本
tokenizer = BertTokenizer.from_pretrained(model_name)
model = BertModel.from_pretrained(model_name)
project_dir = os.getcwd()
# print("项目根目录的绝对路径:", project_dir)
# 加载自定义词典
jieba.set_dictionary(f'{project_dir}\jiebaDict/adjust_dict.txt')
# jieba.load_userdict('jiebaDict/adjust_dict.txt')

def preprocess(text):
    # 使用tokenizer对输入短语进行编码
    inputs = tokenizer.encode_plus(
        text,
        add_special_tokens=True,  # 加入[CLS]和[SEP]标记
        padding='max_length',  # 填充到固定长度
        truncation=True,  # 超过最大长度时截断
        max_length=64,  # 你可以调整max_length的值
        return_tensors='pt'  # 返回PyTorch tensor
    )
    return inputs

def remove_symbols(text):
    # 使用正则表达式去除所有标点符号和其他非字母数字字符
    return re.sub(r'[^\w\s]', '', text)

def get_embeddings(text):
    # 对输入文本进行预处理
    inputs = preprocess(text)
    # 获取模型输出
    with torch.no_grad():
        outputs = model(**inputs)
    # 提取[CLS]位置的嵌入向量作为文本表示（也可以使用其他池化方式）
    embeddings = outputs.last_hidden_state[:, 0, :]  # [CLS] token的嵌入
    return embeddings

def compute_similarity(embedding1, embedding2, text1, text2):
    # 计算两个嵌入向量的余弦相似度
    similarity = cosine_similarity(embedding1.numpy(), embedding2.numpy())[0][0]

    # 计算重复词的权重
    weight = calculate_weight(text1, text2)

    # 调整相似度
    adjusted_similarity = (similarity * (1 - REPETITIONCOEFFICIENT)) + (weight * REPETITIONCOEFFICIENT)
    # adjusted_similarity = similarity

    return adjusted_similarity

def calculate_weight(text1, text2):
    # 去除符号
    text1 = remove_symbols(text1)
    text2 = remove_symbols(text2)

    # 使用jieba进行中文分词
    chinese_words1 = set(jieba.lcut(text1))
    chinese_words2 = set(jieba.lcut(text2))

    # 使用正则表达式提取英文单词
    english_words1 = set(re.findall(r'\b[a-zA-Z]+\b', text1))
    english_words2 = set(re.findall(r'\b[a-zA-Z]+\b', text2))

    # 合并中文和英文词语
    words1 = chinese_words1.union(english_words1)
    words2 = chinese_words2.union(english_words2)

    # 计算重复词的数量
    common_words = words1.intersection(words2)
    # weight = math.sqrt(len(common_words) / (len(words2) + 0.1))
    weight = math.sqrt(len(common_words) / (len(common_words) + 0.5))

    return weight

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python macbert_silmilarity.py <equipment> <classificationString>")
        sys.exit(1)

    equipment = sys.argv[1]
    classificationString = sys.argv[2]

    # 将分类标签字符串分割成列表
    classificationList = classificationString.split(", ")

    # 获取设备名称的嵌入
    word1 = get_embeddings(equipment)

    # 计算每个分类标签与设备名称的相似度
    results = []
    for item in classificationList:
        word2 = get_embeddings(item)
        similarity = compute_similarity(word1, word2, equipment, item)
        results.append((item, similarity))
        # print(f"{item} 相似度: {similarity}")

    # 将结果按照相似度从大到小排序
    results.sort(key=lambda x: x[1], reverse=True)

    # 打印结果
    for item in results:
        print(f"'{item[0]}' \t\t相似度: {item[1]}")
