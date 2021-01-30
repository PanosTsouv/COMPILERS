# -Comment error 1,2,3,4 to check errors from second visitor
# -Second visitor doesn't work if first visitor finds errors

# Error1 Wrong function cals (function is not defined)
# wrong
functionNotDefine()
functionNotDefine1()
def functionNotDefine1():
    # right
    return functionDefine()
# wrong
functionNotDefine1()

def functionDefine():
    print(1)
# right
functionNotDefine1()
functionDefine()

# Error 2 function is already define
def functionExist(a, b = 0, c = 0):
    print(1)

# wrong functions(compare with first)
def functionExist(k):
    print(1)

def functionExist(k, t):
    print(1)

def functionExist(k, t, p):
    print(1)

def functionExist(k, t, p = 0):
    print(1)

def functionExist(k, t = 0, p = 0):
    print(1)

def functionExist(k = 0, t = 0, p = 0):
    print(1)

def functionExist(k, t, p, a = 0, b = 0, c = 0):
    print(1)
#right functions(compare with first)
def functionExist():
    print(1)

def functionExist(k, t, p, a):
    print(1)

# Error 3 function call with different number of arguments

# wrong
functionExist(1,2,3,4,5)

# right
functionExist(1)
functionExist(1,2)
functionExist()
functionExist(1,2,3)
functionExist(1,2,3,4)

# Error 4 default arguments are followed by non default
def args(a=0, b):
    print(1)

def args2(a, b=0, c):
    print(1)

def args3(a=0, b, c):
    print(1)

# Error 5 use not define variables

def add(x,y):
    return x + y
print(k)

def assignV(d = 0):
    l = d
print(l)

print(w)
w = 0
v = a

# Error 6 wrong use of a function(return type)
def voidFunction():
    print(1)

def f(d):
    return d

def x(r):
    return r + f(r)

def y(k, l):
    return x(k) + l

b = y("1","0") + 4

c = voidFunction() + 1

# Error 7 wrong call of a function(add different types)

def add2(x,y):
    return x + y
k="hello world"
print(add2(2,k))

def sub(x,y):
    return x - y
print(sub(2,k))

def mult(x,y):
    return x * y
print(mult(None,k))

def div(x,y):
    return x / y
print(div(2,k))

def mod(x,y):
    return x % y
print(mod(2,k))

def power(x,y):
    return x ** y
print(power(2,k))

def add3(x,y):
    return x + y
r = type(k)
print(add3(2,r))

# this is right (string * number)
def mult2(x,y):
    return x * y
k="hello world"
print(mult2(2,k))

# Error 8 assign error
k=type(k)
k=2

# Error 9 wrong operation with assign
k -= type(k)
k /= 2

# Error 10 wrong passed args
def argsPass(a="a", b=None):
    print(1)
argsPass(1,2)
print("a")

# Error 11 operation with None , open(), type()
print(open("path","r") + open("path","r"))
print(open("path","r") + type(w))
print(None + type(w))
print(None + open("path","r"))
print(1 + open("path","r"))
print("1" + open("path","r"))
print(1 + type(w))
print("None" + type(w))
