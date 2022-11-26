import sys
import os
import json


def print_err(msg, code : int = 1):
    print(msg, sys.stderr)
    sys.exit(code)


def read_file(file_path : str):
    try:
        with open(file_path, 'r') as file:
            return json.load(file)
    except IOError as e:
        print_err('Failed to open file:' + e)


def write_file(file_path : str, content: str):
    try:
        with open(file_path, 'w') as file:
            file.write(content)
    except IOError as e:
        print_err('Failed to open file:' + e)


def tabs(count: int) -> str:
    return '\t' * count


def span(class_str: str, content) -> str:
    return f'<span class="{class_str}">{str(content)}</span>'


def quot(obj) -> str:
    return '"' + str(obj) + '"'


def convert_dict(json_dict: dict, tab_count: int) -> str:
    obj_str = '{\n'
    first = True
    for key in json_dict:
        if not first:
            obj_str += ',\n'
        obj_str += f'{tabs(tab_count)}{span("key", quot(key))}: {convert_helper(json_dict[key], tab_count + 1)}'
        first = False
    return obj_str + '\n' + tabs(tab_count - 1) + '}'


def convert_array(json_array: list, tab_count: int) -> str:
    obj_str = '['
    first = True
    for item in json_array:
        if not first:
            obj_str += ', '
        obj_str += convert_helper(item, tab_count + 1)
        first = False
    return obj_str + ']'


def convert_helper(json_obj, tab_count: int) -> str:
    obj_str = ''
    if json_obj == None:
        obj_str = 'null'
    if isinstance(json_obj, dict):
        obj_str += convert_dict(json_obj, tab_count)
    elif isinstance(json_obj, list):
        obj_str += convert_array(json_obj, tab_count)
    elif isinstance(json_obj, int) or isinstance(json_obj, float):
        obj_str += span('num', json_obj)
    elif isinstance(json_obj, bool):
        obj_str += span('num', 'true' if json_obj else 'false')
    else:
        obj_str += span('str', quot(json_obj))

    return obj_str   


def convert_json(json_data: dict) -> str:
    return f'<pre>\n{convert_helper(json_data, 1)}\n</pre>'


def main():
    if len(sys.argv) != 3:
        print_err('Error: program takes two argument argument')

    
    file_str = convert_json(read_file(sys.argv[1]))
    write_file(sys.argv[2], file_str)
    print('converted successfully')
    

if __name__ == '__main__':
    main()