################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../graphics/draw_font.c \
../graphics/fill.c \
../graphics/graphics.c \
../graphics/menu.c 

OBJS += \
./graphics/draw_font.o \
./graphics/fill.o \
./graphics/graphics.o \
./graphics/menu.o 

C_DEPS += \
./graphics/draw_font.d \
./graphics/fill.d \
./graphics/graphics.d \
./graphics/menu.d 


# Each subdirectory must supply rules for building sources it contributes
graphics/%.o: ../graphics/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C Compiler'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


