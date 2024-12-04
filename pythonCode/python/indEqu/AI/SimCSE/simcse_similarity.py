from sentence_transformers import SentenceTransformer, util

def calculate_similarity(text1, text2, model_name='simcse-bert-base-chinese'):
    model = SentenceTransformer(model_name)
    embeddings1 = model.encode(text1, convert_to_tensor=True)
    embeddings2 = model.encode(text2, convert_to_tensor=True)
    cosine_scores = util.pytorch_cos_sim(embeddings1, embeddings2)
    return cosine_scores.item()

if __name__ == "__main__":
    import sys
    if len(sys.argv) != 3:
        print("Usage: python simcse_similarity.py <text1> <text2>")
        sys.exit(1)

    text1 = sys.argv[1]
    text2 = sys.argv[2]
    similarity = calculate_similarity(text1, text2)
    print(similarity)