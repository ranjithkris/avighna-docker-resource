import os

# Avighna related path
AVIGHNA_AGENT = './avighna-agent-1.0.0.jar'
AVIGHNA_CMD_INTERFACE = './avighna-cmd-interface-1.0.0.jar'
AVIGHNA_ROOT_OUTPUT_DIR = './avighna-output/'

# Root package for the projects that we used in the evaluation
PETCLINIC_ROOT_PCK = 'org.springframework.samples.petclinic'


def main():
    os.makedirs(AVIGHNA_ROOT_OUTPUT_DIR)

    print("Welcome to Avighna")
    print("Select on which type of applications you want to run Avighna?")
    print("1. Spring Applications")
    print("2. Guice Applications")
    print("3. Java Reflection Applications")

    try:
        a = int(input("Please enter your choice: "))
        print(a)
    except ValueError:
        print("Invalid options")


if __name__ == '__main__':
    main()
