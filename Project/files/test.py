# -Comment error 1,2,3 to check errors from second visitor
# -Second visitor doesn't work if first visitor finds errors

# Error1 Wrong function cals (function is not defined)
# wrong
functionNotDefine()
functionNotDefine1()
def functionNotDefine1():
    # right
    return functionDefine()

def functionDefine():
    print(1)
# right
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

# Error 4 use not define variables

def add(x,y):
    return x + y
print(k)

def assignV(d = 0):
    l = d
print(l)

print(w)
w = 0
v = a

# Error 5 wrong use of a function(return type)
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

# Error 6 wrong call of a function(add different types)

def add2(x,y):
    return x + y
k="hello world"
print(add2(2,k))

# this is right (string * number)
def mult2(x,y):
    return x * y
k="hello world"
print(mult2(2,k))