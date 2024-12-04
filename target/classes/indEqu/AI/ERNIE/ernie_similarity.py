from transformers import BertTokenizer, BertModel
import torch
import numpy as np  # 导入 numpy 库

# 加载 ERNIE 模型和分词器
model_name = 'nghuyong/ernie-1.0'
tokenizer = BertTokenizer.from_pretrained(model_name,
                                          token="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")
model = BertModel.from_pretrained(model_name,
                                  token="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")
def get_embedding(text):
    inputs = tokenizer(text, return_tensors='pt')
    outputs = model(**inputs)
    return outputs.pooler_output.squeeze().detach().numpy()

def cosine_similarity(vec1, vec2):
    dot_product = np.dot(vec1, vec2)
    norm_vec1 = np.linalg.norm(vec1)
    norm_vec2 = np.linalg.norm(vec2)
    return dot_product / (norm_vec1 * norm_vec2)

# 示例文本
text1 = "M12 防水连接器"
text2 = "M12 防水连接器"
text3 = "English"
text4 = "毫不相关"
text5 = "工业电源"
text6 = "工业连接器"
text7 = "称紧凑型外围组件互连扩展CompactPCI-面向仪器系统的外围组件互连扩展PXI"
text8 = "执行机构"


embedding1 = get_embedding(text1)
embedding2 = get_embedding(text2)
embedding3 = get_embedding(text3)
embedding4 = get_embedding(text4)
embedding5 = get_embedding(text5)
embedding6 = get_embedding(text6)
embedding7 = get_embedding(text7)
embedding8 = get_embedding(text8)

similarity = cosine_similarity(embedding1, embedding2)
print(f"{text2}相似度: {similarity}")
similarity = cosine_similarity(embedding1, embedding3)
print(f"{text3}相似度: {similarity}")
similarity = cosine_similarity(embedding1, embedding4)
print(f"{text4}相似度: {similarity}")
similarity = cosine_similarity(embedding1, embedding5)
print(f"{text5}相似度: {similarity}")
similarity = cosine_similarity(embedding1, embedding6)
print(f"{text6}相似度: {similarity}")
similarity = cosine_similarity(embedding1, embedding7)
print(f"{text7}相似度: {similarity}")
similarity = cosine_similarity(embedding1, embedding8)
print(f"{text8}相似度: {similarity}")