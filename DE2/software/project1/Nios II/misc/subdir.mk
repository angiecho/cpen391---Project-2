################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../misc/b_tree.c \
../misc/hashmap.c \
../misc/misc_helpers.c 

OBJS += \
./misc/b_tree.o \
./misc/hashmap.o \
./misc/misc_helpers.o 

C_DEPS += \
./misc/b_tree.d \
./misc/hashmap.d \
./misc/misc_helpers.d 


# Each subdirectory must supply rules for building sources it contributes
misc/%.o: ../misc/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C Compiler'
	gcc -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


