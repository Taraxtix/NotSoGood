# NotSoGood
Dummy Programming language made for learning purpose. 

NotSoGood is a stack based language inspired by [Porth](https://gitlab.com/tsoding/porth) (itself inspired by [Forth](https://fr.wikipedia.org/wiki/Forth_(langage)))

- [x] Simulated in Java. 
- [x] Compiled into native assembly. 
- [ ] Self-Hosted.
- [ ] Statically typed.
- [ ] Optimized.
- [ ] Portable (Generated assembly for all kind of architectures).

## Builtin operation

### -- PUSH -- 
`x` Pushes x on top of the stack (x must be an integer)

### -- PRINT --
`print` Print the value on top of the stack into the standard output

### -- DUP --
`dup` Duplicate the value on the top of the stack
```
int a = pop()
push(a)
push(a)
```

### -- DROP --
`drop` Delete the value on top of the stack
```
pop()
```

### -- ARITHMETICS --

#### -- PLUS --

`+` Adds the two integers at the top of the stack and push the result

```
int a = pop()
int b = pop()
push(b + a)
```

#### -- MINUS --

`-` Subtract the two integers at the top of the stack and push the result

```
int a = pop()
int b = pop()
push(b - a)
```

#### -- MULTIPLICATION --

`*` Multiply the two integers at the top of the stack and push the result

```
int a = pop()
int b = pop()
push(b * a)
```

### -- CONDITIONAL OPERATION --

### -- EQUAL --
`=`
```
int a = pop()
int b = pop()
if(b == a) { push(1) }
else { push(0) }
```

### -- UNEQUAL --
`!=`
```
int a = pop()
int b = pop()
if(b != a) { push(1) }
else { push(0) }
```

### -- LESS --
`<`
```
int a = pop()
int b = pop()
if(b < a) { push(1) }
else { push(0) }
```

### -- GREATER --
`>`
```
int a = pop()
int b = pop()
if(b > a) { push(1) }
else { push(0) }
```

### -- LESS OR EQUAL --
`<=`
```
int a = pop()
int b = pop()
if(b <= a) { push(1) }
else { push(0) }
```

### -- GREATER OR EQUAL --
`>=`
```
int a = pop()
int b = pop()
if(b >= a) { push(1) }
else { push(0) }
```

### -- CONTROL FLOW --

#### -- ELSE-LESS IF --

`do` Consume the value on the top of the stack (If it is false (0) it goes to the first instruction after the `end`
keyword otherwise it go to the next instruction)
```
if cond do
    ...
end
```

#### -- IF-ELSE --

`do` Consume the value on the top of the stack (If it is false (0) it goes to the first instruction after the `else`
keyword otherwise it go to the next instruction)
`else` always jump to the first instruction after the `end` keyword

```
if cond do
    ...
else
    ...
end
```

#### -- WHILE --

`do` Consume the value on the top of the stack (If it is false (0) it goes to the first instruction after the `end`
keyword otherwise it go to the next instruction)
`end` always jumps to the first instruction after the `while` keyword

```
while cond do
    ...
end
```

### -- MEMORY MANAGEMENT --

#### -- MEM --

`mem` Pushes the pointer to the start of the memory into the stack

#### -- STORE8 --

`store8` Stores a 8bit value to a specific location of the memory

```
int value = pop()
int pointer = pop()
*pointer = value 
```

#### -- LOAD8 --

`load8` Push the value at the given pointer to the stack

```
int pointer = pop()
push(*pointer)
```