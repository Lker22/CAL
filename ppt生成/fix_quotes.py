# -*- coding: utf-8 -*-
with open('generate_doc.py', 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('“', '「')  # left curly quote -> left corner bracket
content = content.replace('”', '」')  # right curly quote -> right corner bracket
with open('generate_doc.py', 'w', encoding='utf-8') as f:
    f.write(content)
print('Done')
